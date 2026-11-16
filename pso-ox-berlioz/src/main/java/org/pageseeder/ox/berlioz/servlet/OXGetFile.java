/*
 * Copyright 2026 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.ox.berlioz.servlet;

import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.berlioz.util.NonceUtils;
import org.pageseeder.ox.util.FileUtils;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>Serves a file stored inside the OX temp folder (Packages). Requires JDK 11+.</p>
 *
 * <p>The servlet is <b>fail-closed</b>: a file is served only when every check passes. If any check
 * fails, cannot be evaluated, or the configuration is empty/invalid, the request is refused.</p>
 *
 * <p>Init parameters:</p>
 * <ul>
 *   <li><code>downloadable</code>: <code>true</code> sends the file as an attachment, anything else
 *       sends it inline.</li>
 *   <li><code>extesions-allowed</code> (or the correctly spelled <code>extensions-allowed</code>):
 *       comma separated allow list of extensions. When missing or blank,
 *       {@link #DEFAULT_EXTENSIONS_ALLOWED} is used. Any extension not in the list is refused.</li>
 *   <li><code>pattern</code>: no longer used (the file is taken from the servlet path info); it may
 *       stay in <code>web.xml</code> without effect.</li>
 * </ul>
 *
 * <p>Hardening applied:</p>
 * <ul>
 *   <li>The file is identified ONLY by the path info, never by query parameters. Requests carrying
 *       <code>path</code>, <code>name</code>, <code>file</code> or <code>download</code> are rejected.</li>
 *   <li>Path parameters (<code>;.css</code>), encoded separators/dots, double encoding, <code>..</code>,
 *       <code>//</code>, <code>:</code> (e.g. <code>https:/</code>), backslashes and control characters
 *       are rejected.</li>
 *   <li>Every path segment is checked against a strict whitelist.</li>
 *   <li>The real path (symlinks resolved) must stay inside the OX temp folder.</li>
 *   <li>The extension is checked case-insensitively on the final segment against an allow list.</li>
 *   <li>Safe response headers (nosniff, no-store, RFC 6266 filename).</li>
 * </ul>
 *
 * @author
 * @version 16 Nov 2026 (hardened, fail-closed, JDK 11)
 */
public final class OXGetFile extends HttpServlet {

  /** Serialization id, kept from the original class so existing serialized state stays compatible. */
  private static final long serialVersionUID = -7694484458213953462L;

  /** Logger used to record rejected requests and configuration problems. */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXGetFile.class);

  /**
   * Extensions allowed when the servlet has no <code>extesions-allowed</code> init parameter.
   * A safe default means a forgotten configuration can never expose arbitrary file types.
   */
  private static final String DEFAULT_EXTENSIONS_ALLOWED =
      ".zip,.xml,.json,.pdf,.csv,.xlsx,.docx,.txt,.html,.png,.jpg,.jpeg";

  /**
   * Query parameters that must never be present. Some clients/filters/frameworks let such parameters
   * override the requested file, so any request carrying them is treated as an attack attempt.
   */
  private static final Set<String> FORBIDDEN_PARAMETERS = Set.of("path", "name", "file", "download");

  /**
   * Percent-encoded characters that are never legitimate in the raw URI of this servlet (compared in
   * lowercase): encoded dot, slash, backslash, semicolon, NUL, percent (double encoding) and colon.
   * They are the usual building blocks of traversal and filter-bypass attacks.
   */
  private static final String[] FORBIDDEN_ENCODED = {
      "%2e", "%2f", "%5c", "%3b", "%00", "%25", "%3a"
  };

  /**
   * One path segment: must start with a letter, digit or underscore (so no ".", ".." or hidden files)
   * and may then contain letters, digits, underscore, dot, hyphen and space, up to 255 characters
   * (the usual file system limit). Anything else (separators, ';', ':', control chars) is refused.
   */
  private static final Pattern SAFE_SEGMENT = Pattern.compile("^[A-Za-z0-9_][A-Za-z0-9_.\\- ]{0,254}$");

  /**
   * Largest HTML file rewritten in memory to add the CSP nonce. A limit prevents a huge file from
   * exhausting the heap (denial of service); larger files are streamed unmodified instead.
   */
  private static final long MAX_HTML_REWRITE_BYTES = 5L * 1024 * 1024;

  /**
   * Accepted nonce format (base64 / URL-safe base64). Validating it guarantees the value cannot break
   * out of the HTML attribute it is written into (attribute / markup injection).
   */
  private static final Pattern SAFE_NONCE = Pattern.compile("^[A-Za-z0-9+/=_\\-]{8,128}$");

  /**
   * Matches every <code>&lt;script</code> start tag that does not already carry a nonce. The look-ahead
   * avoids matching other tags such as <code>&lt;scripts</code> and the negative look-ahead avoids
   * adding a second nonce attribute. Case-insensitive because HTML tags are.
   */
  private static final Pattern SCRIPT_TAG =
      Pattern.compile("<script(?![^>]*\\snonce\\s*=)(?=[\\s>/])", Pattern.CASE_INSENSITIVE);

  /**
   * Lowercase extensions (without the dot) that may be served. Set once in {@link #init(ServletConfig)}
   * and never modified afterwards, so it is safe to share between request threads. If it ends up
   * empty, every request is refused (fail-closed).
   */
  private transient Set<String> allowedExtensions = Set.of();

  /**
   * <code>true</code> to send the file as an attachment (download), <code>false</code> to display it
   * inline (view). Set once in {@link #init(ServletConfig)} for the same thread-safety reason.
   */
  private transient boolean downloadable;

  /**
   * Supplies the OX temp folder. Defaults to {@link OXConfig#getOXTempFolder()}; replaceable in unit
   * tests so no static mocking (PowerMock) is needed.
   */
  private transient Supplier<File> tempFolderProvider = OXConfig::getOXTempFolder;

  /**
   * Supplies the CSP nonce of a request. Defaults to {@link NonceUtils#getNonce(HttpServletRequest)};
   * replaceable in unit tests for the same reason as {@link #tempFolderProvider}.
   */
  private transient Function<HttpServletRequest, String> nonceProvider = NonceUtils::getNonce;

  /**
   * Test seam: replaces the temp folder supplier.
   *
   * @param provider the supplier of the folder files are served from
   */
  void setTempFolderProvider(Supplier<File> provider) {
    this.tempFolderProvider = provider;
  }

  /**
   * Test seam: replaces the nonce supplier.
   *
   * @param provider the function returning the nonce for a request (may return null)
   */
  void setNonceProvider(Function<HttpServletRequest, String> provider) {
    this.nonceProvider = provider;
  }

  /**
   * Reads the servlet configuration once, when the container starts the servlet.
   *
   * <p>Doing this in <code>init</code> instead of on each request avoids a race condition (several
   * threads writing the same fields) and avoids parsing the configuration for every request.</p>
   *
   * @param config the servlet configuration provided by the container
   * @throws ServletException if the superclass cannot be initialised
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    // The superclass must be initialised first so getServletName() and the logging below work
    super.init(config);

    // Support the legacy misspelled name so existing web.xml files keep working
    String configured = config.getInitParameter("extesions-allowed");
    // Fall back to the correctly spelled name for new configurations
    if (StringUtils.isBlank(configured)) configured = config.getInitParameter("extensions-allowed");
    // A missing/blank value means "use the safe default" rather than "allow everything"
    String list = StringUtils.isBlank(configured) ? DEFAULT_EXTENSIONS_ALLOWED : configured;

    // Normalise: trim spaces, lowercase and drop the leading dot so comparisons are consistent
    this.allowedExtensions = Arrays.stream(list.split(","))
        .map(e -> e.trim().toLowerCase(Locale.ROOT))
        .map(e -> e.startsWith(".") ? e.substring(1) : e)
        // Ignore empty entries such as the one produced by a trailing comma
        .filter(e -> !e.isEmpty())
        // Immutable copy: it can never be altered after start-up
        .collect(Collectors.toUnmodifiableSet());

    // Only the exact value "true" (any case) enables attachments; anything else stays inline
    this.downloadable = "true".equalsIgnoreCase(config.getInitParameter("downloadable"));

    // An explicit but unusable list (e.g. ",") leaves nothing allowed: warn so the admin notices
    if (this.allowedExtensions.isEmpty()) {
      LOGGER.warn("Servlet {} has an empty extension allow list: ALL requests will be refused.", getServletName());
    }
  }

  /**
   * Serves the requested file after every validation has passed. Each check refuses the request as
   * soon as it fails, so no later step ever runs on untrusted input.
   *
   * @param req the request; the file is identified by its path info only
   * @param res the response receiving the file or an error status
   * @throws ServletException never thrown here, declared by the servlet API
   * @throws IOException if writing the response fails
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    // 1. Query parameters and raw URI are checked first because the decoded path hides these tricks
    if (hasForbiddenParameter(req) || !isRawUriSafe(req.getRequestURI())) {
      // 400: the request itself is malformed/hostile, it is not a missing file
      reject(res, HttpServletResponse.SC_BAD_REQUEST, "bad request", req.getRequestURI());
      return;
    }

    // 2. Path info is relative to the servlet mapping and already decoded once by the container,
    //    so it does not depend on the mapping prefix (no fragile lastIndexOf on the raw URI)
    String relative = req.getPathInfo();
    String[] segments = toSafeSegments(relative);
    if (segments == null) {
      // 400: traversal, scheme, hidden file or otherwise invalid path
      reject(res, HttpServletResponse.SC_BAD_REQUEST, "invalid path", relative);
      return;
    }

    // 3. The extension is checked on the last segment only, so "x.css;.xml" style tricks cannot help
    String fileName = segments[segments.length - 1];
    if (!isExtensionAllowed(fileName)) {
      // 403: the file may exist but this type is not allowed to leave the server
      reject(res, HttpServletResponse.SC_FORBIDDEN, "extension not allowed", fileName);
      return;
    }

    // 4. Resolve the real location and verify it is inside the OX temp folder (symlinks included)
    Path file = resolveInsideBase(segments);
    // (resolveInsideBase reads the base folder from tempFolderProvider)
    if (file == null || !Files.isRegularFile(file) || !Files.isReadable(file)) {
      // 404 for outside, missing, directory or unreadable alike, so the response leaks nothing
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // 5. Content type from the file name; unknown types become a generic binary type
    String mediaType = FileUtils.getMimeType(file.toFile());
    boolean unknown = mediaType == null || "unknown".equals(mediaType);
    res.setContentType(unknown ? "application/octet-stream" : mediaType);
    // Stops browsers from guessing a different (executable) type than the one declared
    res.setHeader("X-Content-Type-Options", "nosniff");
    // Package content can be private or short lived, so shared caches must not keep it
    res.setHeader("Cache-Control", "no-store");
    // Attachment vs inline, with a filename that cannot inject header content
    res.setHeader("Content-Disposition", contentDisposition(fileName));

    // Size is needed for Content-Length and for the HTML rewrite limit
    long size = Files.size(file);
    // The nonce is per request, so the CSP of the page and the scripts in the file can match
    String nonce = nonceProvider.apply(req);
    // startsWith so "text/html; charset=UTF-8" is also recognised as HTML
    boolean isHtml = !unknown && mediaType.toLowerCase(Locale.ROOT).startsWith("text/html");

    // 6. Rewrite scripts only for HTML, with a valid nonce, and when the file is small enough
    if (isHtml && nonce != null && SAFE_NONCE.matcher(nonce).matches() && size <= MAX_HTML_REWRITE_BYTES) {
      // readAllBytes with an explicit charset: unlike Files.readString it does not throw on
      // invalid UTF-8 (which would turn a bad file into a 500 error)
      String html = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
      // quoteReplacement: the nonce is already validated, but a replacement string is never trusted
      byte[] out = SCRIPT_TAG.matcher(html)
          .replaceAll(Matcher.quoteReplacement("<script nonce=\"" + nonce + "\""))
          .getBytes(StandardCharsets.UTF_8);
      // Declare the charset actually used to encode the body
      res.setCharacterEncoding("UTF-8");
      // The length changed after the rewrite, so it is computed from the new bytes
      res.setContentLength(out.length);
      res.getOutputStream().write(out);
    } else {
      // Exact length lets clients detect truncated downloads; the long variant supports big files
      res.setContentLengthLong(size);
      // try-with-resources closes the file even when the client disconnects half way
      try (var in = Files.newInputStream(file)) {
        // Copies until end of stream (the original loop stopped at the first short read and could
        // truncate files)
        in.transferTo(res.getOutputStream());
      }
      // Make sure the last buffered bytes reach the client
      res.getOutputStream().flush();
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Validation helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Tells whether the request carries a query parameter that could be used to override the file
   * (<code>path</code>, <code>name</code>, <code>file</code> or <code>download</code>).
   *
   * @param req the request to inspect
   * @return <code>true</code> if a forbidden parameter is present (names compared in lowercase)
   */
  private static boolean hasForbiddenParameter(HttpServletRequest req) {
    // Lowercase compare so "PATH" or "File" cannot slip through; a repeated parameter is also caught
    return Collections.list(req.getParameterNames()).stream()
        .anyMatch(n -> FORBIDDEN_PARAMETERS.contains(n.toLowerCase(Locale.ROOT)));
  }

  /**
   * Checks the raw (not decoded) URI for path parameters and suspicious encodings.
   *
   * <p>This must run on the raw URI: once the container decodes the path, <code>%3b</code> looks like
   * a plain ';' and <code>%252e</code> looks like a harmless '%2e', and the evidence is lost.</p>
   *
   * @param rawUri the value of {@link HttpServletRequest#getRequestURI()}
   * @return <code>true</code> if the URI is acceptable; <code>false</code> for null, ';' (matrix
   *         parameters such as <code>;.css</code>), backslash, or any forbidden encoded character
   */
  private static boolean isRawUriSafe(String rawUri) {
    // No URI means nothing can be validated: refuse (fail-closed)
    if (rawUri == null) return false;
    // ';' starts a path parameter that some filters use to fake an allowed extension; '\' is a
    // directory separator on Windows
    if (rawUri.indexOf(';') >= 0 || rawUri.indexOf('\\') >= 0) return false;
    // Lowercase once so %2E and %2e are treated the same
    String lower = rawUri.toLowerCase(Locale.ROOT);
    for (String bad : FORBIDDEN_ENCODED) {
      // Any encoded dot/slash/backslash/semicolon/NUL/percent/colon is refused outright
      if (lower.contains(bad)) return false;
    }
    return true;
  }

  /**
   * Splits the decoded path info into validated segments.
   *
   * <p>Expected shape: <code>/&lt;package-id&gt;/&lt;file or folders...&gt;</code>. Using an
   * allow-list of characters (rather than trying to block known bad ones) is what blocks
   * <code>../</code>, <code>https:/host/...</code>, hidden files and Windows tricks.</p>
   *
   * @param pathInfo the decoded path after the servlet mapping, may be null
   * @return the validated segments, or <code>null</code> if the path is not acceptable
   */
  private static String[] toSafeSegments(String pathInfo) {
    // Must exist, have at least one character after '/', and start with '/' as the container provides
    if (pathInfo == null || pathInfo.length() < 2 || pathInfo.charAt(0) != '/') return null;
    // Overlong paths are never legitimate; a trailing '/' means a folder, not a file
    if (pathInfo.length() > 1024 || pathInfo.endsWith("/")) return null;
    // Empty segments: collapses "https://" and "a//b" tricks that normalisation could hide
    if (pathInfo.contains("//")) return null;
    for (int i = 0; i < pathInfo.length(); i++) {
      char c = pathInfo.charAt(i);
      // Control chars (incl. NUL/CR/LF), backslash, ';', ':' (schemes, drive letters, NTFS streams),
      // '?', '%' (leftover encoding = double encoding) and '*' (wildcards) are never allowed
      if (c < 0x20 || c == 0x7f || c == '\\' || c == ';' || c == ':' || c == '?' || c == '%' || c == '*') return null;
    }
    // Limit -1 keeps trailing empty strings, so they fail the segment test below
    String[] segments = pathInfo.substring(1).split("/", -1);
    // A file always lives under a package folder: <package-id>/<file>
    if (segments.length < 2) return null;
    for (String s : segments) {
      // Whitelist per segment: rejects "", ".", ".." and hidden files starting with '.'
      if (!SAFE_SEGMENT.matcher(s).matches()) return null;
      // Windows silently drops trailing dots/spaces, so "a.xml." could map to a different file
      if (s.endsWith(".") || s.endsWith(" ")) return null;
    }
    return segments;
  }

  /**
   * Checks the file extension against the allow list (fail-closed).
   *
   * @param fileName the last path segment
   * @return <code>true</code> only if the extension is in the allow list; <code>false</code> if there
   *         is no extension, if it is not listed, or if the allow list is empty
   */
  private boolean isExtensionAllowed(String fileName) {
    // Position of the last dot decides the extension (so "a.b.xml" is "xml")
    int dot = fileName.lastIndexOf('.');
    // No extension, or a name ending in '.', cannot be matched to the allow list: refuse
    if (dot < 0 || dot == fileName.length() - 1) return false;
    // Lowercase so ".XML" and ".xml" behave the same
    String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    // Fail-closed: an empty list allows nothing, and only listed extensions are served
    return allowedExtensions.contains(ext);
  }

  /**
   * Resolves the file inside the OX temp folder and confirms it cannot escape it.
   *
   * <p>The check is done twice: on the normalised path (stops <code>..</code>) and on the real path
   * (stops symbolic links that point outside the folder).</p>
   *
   * @param segments the validated path segments
   * @return the real path of the file, or <code>null</code> if it is outside the folder, does not
   *         exist, or cannot be resolved
   */
  private Path resolveInsideBase(String[] segments) {
    try {
      // Real path of the base: removes symlinks so the later startsWith comparison is reliable
      Path base = Path.of(tempFolderProvider.get().getPath()).toRealPath();
      Path candidate = base;
      // Segment by segment so a segment can never be interpreted as an absolute path
      for (String s : segments) candidate = candidate.resolve(s);
      // Collapse any remaining "." / ".." (already blocked, kept as defence in depth)
      candidate = candidate.normalize();
      // First containment check, cheap and before touching the file system
      if (!candidate.startsWith(base)) return null;
      // Follows symlinks and fails if the file does not exist
      Path real = candidate.toRealPath();
      // Second containment check: a symlink inside the folder must not lead outside it
      return real.startsWith(base) ? real : null;
    } catch (NoSuchFileException e) {
      // Normal case for a wrong name: not an error worth logging
      return null;
    } catch (IOException | RuntimeException e) {
      // Fail-closed: any unexpected problem means "do not serve"; log only the summary
      LOGGER.warn("Cannot resolve file: {}", e.toString());
      return null;
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Response helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Builds the <code>Content-Disposition</code> header (RFC 6266 / RFC 5987).
   *
   * <p>An ASCII-only fallback <code>filename</code> and an encoded <code>filename*</code> are both
   * sent, so quotes, CR/LF or non-ASCII characters in the name cannot break or inject into the
   * header, and modern browsers still show the real name.</p>
   *
   * @param fileName the (already validated) file name
   * @return the header value
   */
  private String contentDisposition(String fileName) {
    // "attachment" forces a download, "inline" lets the browser display the file
    String type = downloadable ? "attachment" : "inline";
    // Fallback name for old clients: every character outside a safe set becomes '_'
    String ascii = fileName.replaceAll("[^A-Za-z0-9._\\- ]", "_");
    // Percent-encoded UTF-8 name; URLEncoder uses '+' for spaces but RFC 5987 requires %20
    String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    return type + "; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded;
  }

  /**
   * Logs a rejected request and sets the response status. No body is written so the response reveals
   * nothing about the server.
   *
   * @param res the response
   * @param status the HTTP status to send
   * @param reason short reason, for the log only
   * @param value the offending value, for the log only (sanitised)
   */
  private static void reject(HttpServletResponse res, int status, String reason, String value) {
    // Rejections are logged as warnings so attacks can be detected and alerted on
    LOGGER.warn("Request rejected ({}): {}", reason, sanitizeForLog(value));
    // setStatus (not sendError) avoids the container's default error page with version information
    res.setStatus(status);
  }

  /**
   * Makes an untrusted value safe to write in a log line.
   *
   * @param value the value from the request, may be null
   * @return the value cut to 200 characters with CR, LF and tab replaced by '_', which prevents log
   *         forging (fake log lines) and log flooding
   */
  private static String sanitizeForLog(String value) {
    // Explicit text instead of a NullPointerException
    if (value == null) return "null";
    // Cap the size so one request cannot flood the log
    String s = value.length() > 200 ? value.substring(0, 200) + "..." : value;
    // Line breaks would let an attacker inject fake log entries
    return s.replaceAll("[\\r\\n\\t]", "_");
  }
}
