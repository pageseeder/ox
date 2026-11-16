/*
 * Copyright 2021 Allette Systems (Australia)
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

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OXGetFile}.
 *
 * <p>Uses JUnit 4 and plain Mockito only (no PowerMock): the two static dependencies (temp folder and
 * nonce) are replaced through the servlet's package-private test seams, and real files are created in
 * a {@link TemporaryFolder}.</p>
 */
public class OXGetFileTest {

  /** A realistic package id, the first path segment of every request. */
  private static final String ID = "PBS-WEBSITE-E4C746-A685";

  /** Valid nonce (base64 style, 8+ characters). */
  private static final String NONCE = "dGVzdG5vbmNlMTIz";

  /** Creates (and removes after each test) the temp folders and files used by the tests. */
  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  /** Folder acting as the OX temp folder. */
  private File base;

  /** The servlet under test, configured with the defaults in {@link #setUp()}. */
  private OXGetFile servlet;

  /** Creates the base folder and a servlet that serves from it with no nonce. */
  @Before
  public void setUp() throws Exception {
    base = tmp.newFolder("ox-temp");
    servlet = new OXGetFile();
    servlet.setTempFolderProvider(() -> base);
    servlet.setNonceProvider(req -> null);
    // Default configuration: no init parameters (default extension list, inline)
    init(null, null);
  }

  // ---------------------------------------------------------------------------------------------
  // Successful requests
  // ---------------------------------------------------------------------------------------------

  /** An allowed file is served with its exact content and the safe headers. */
  @Test
  public void servesAllowedFile() throws Exception {
    byte[] content = "<a>hello</a>".getBytes(StandardCharsets.UTF_8);
    writeFile(ID + "/list/files.xml", content);

    Result r = get("/" + ID + "/list/files.xml");

    assertEquals(200, r.status);
    assertArrayEquals(content, r.body);
    assertEquals("nosniff", r.headers.get("X-Content-Type-Options"));
    assertEquals("no-store", r.headers.get("Cache-Control"));
    assertEquals(content.length, r.contentLength);
  }

  /** Content is sent inline when downloadable is not "true". */
  @Test
  public void sendsInlineByDefault() throws Exception {
    writeFile(ID + "/a.xml", "x".getBytes());

    Result r = get("/" + ID + "/a.xml");

    assertTrue(r.headers.get("Content-Disposition").startsWith("inline;"));
    assertTrue(r.headers.get("Content-Disposition").contains("filename=\"a.xml\""));
  }

  /** Content is an attachment when downloadable is "true". */
  @Test
  public void sendsAttachmentWhenDownloadable() throws Exception {
    init(null, "true");
    writeFile(ID + "/a.xml", "x".getBytes());

    Result r = get("/" + ID + "/a.xml");

    assertTrue(r.headers.get("Content-Disposition").startsWith("attachment;"));
  }

  /** Spaces in the name are kept in the fallback and percent-encoded (%20, not +) in filename*. */
  @Test
  public void encodesSpacesInContentDisposition() throws Exception {
    writeFile(ID + "/my file.xml", "x".getBytes());

    Result r = get("/" + ID + "/my file.xml");

    String header = r.headers.get("Content-Disposition");
    assertTrue(header, header.contains("filename=\"my file.xml\""));
    assertTrue(header, header.contains("filename*=UTF-8''my%20file.xml"));
  }

  /** Regression: the original loop stopped at the first short read and could truncate files. */
  @Test
  public void streamsBinaryFilesWithoutTruncation() throws Exception {
    int[] sizes = {1, 2047, 2048, 2049, 4096, 10_000, 100_001};
    for (int size : sizes) {
      byte[] content = new byte[size];
      for (int i = 0; i < size; i++) content[i] = (byte) (i * 31 + 7);
      writeFile(ID + "/data" + size + ".zip", content);

      Result r = get("/" + ID + "/data" + size + ".zip");

      assertEquals("status for size " + size, 200, r.status);
      assertArrayEquals("content for size " + size, content, r.body);
    }
  }

  /** The extension check is case-insensitive. */
  @Test
  public void extensionCheckIsCaseInsensitive() throws Exception {
    writeFile(ID + "/UPPER.XML", "x".getBytes());

    assertEquals(200, get("/" + ID + "/UPPER.XML").status);
  }

  // ---------------------------------------------------------------------------------------------
  // Query parameters and raw URI
  // ---------------------------------------------------------------------------------------------

  /** Requests carrying path/name/file/download parameters are rejected, in any case. */
  @Test
  public void rejectsForbiddenQueryParameters() throws Exception {
    writeFile(ID + "/x.css", "x".getBytes());
    writeFile("data/workbook.xml", "secret".getBytes());
    String[] names = {"path", "name", "file", "download", "PATH", "File", "DownLoad"};

    for (String name : names) {
      Result r = call(request("/bzox/download/" + ID + "/x.css", "/" + ID + "/x.css", name));
      assertEquals("parameter " + name, 400, r.status);
      assertEquals("parameter " + name, 0, r.body.length);
    }
  }

  /** Unrelated query parameters are fine (for example cache busting). */
  @Test
  public void allowsHarmlessQueryParameters() throws Exception {
    writeFile(ID + "/a.xml", "x".getBytes());

    Result r = call(request("/bzox/view/" + ID + "/a.xml", "/" + ID + "/a.xml", "v"));

    assertEquals(200, r.status);
  }

  /** Path parameters (;.css), encoded separators/dots and double encoding are rejected. */
  @Test
  public void rejectsSuspiciousRawUris() throws Exception {
    writeFile(ID + "/xml/document.xml", "x".getBytes());
    String[] uris = {
        "/bzox/view/" + ID + "/xml/document.xml;.css",
        "/bzox/view/" + ID + ";.css/xml/document.xml",
        "/bzox/view/" + ID + "/data/workbook.xml;.js",
        "/bzox/view/" + ID + "/%2e%2e/x.xml",
        "/bzox/view/" + ID + "/%2E%2E/x.xml",
        "/bzox/view/" + ID + "%2fxml/document.xml",
        "/bzox/view/" + ID + "%5cxml/document.xml",
        "/bzox/view/" + ID + "/xml/document.xml%3b.css",
        "/bzox/view/" + ID + "/xml/document.xml%00.css",
        "/bzox/view/" + ID + "/%252e%252e/x.xml",
        "/bzox/view/" + ID + "/xml\\document.xml",
    };

    for (String uri : uris) {
      // The decoded path info is deliberately valid: only the raw URI check can catch these
      Result r = call(request(uri, "/" + ID + "/xml/document.xml"));
      assertEquals(uri, 400, r.status);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Path validation
  // ---------------------------------------------------------------------------------------------

  /** Traversal, schemes, hidden files, empty segments and other invalid shapes are rejected. */
  @Test
  public void rejectsInvalidPaths() throws Exception {
    String[] paths = {
        null,
        "",
        "/",
        "/" + ID,                                   // no file
        "/" + ID + "/",                             // folder
        "/" + ID + "//a.xml",                       // empty segment
        "/" + ID + "/../a.xml",
        "/../../bin/src",
        "/" + ID + "/./a.xml",
        "/" + ID + "/list/https:/webhook.site/e786ea17/t1-list.xml",
        "/" + ID + "/list/https://webhook.site/t1-list.xml",
        "/" + ID + "/.hidden.xml",
        "/" + ID + "/a.xml.",                       // trailing dot (Windows)
        "/" + ID + "/a.xml ",                       // trailing space (Windows)
        "/" + ID + "/a%2exml",                      // leftover encoding
        "/" + ID + "/a;.css",
        "/" + ID + "/a*.xml",
        "/" + ID + "/a\\b.xml",
        "/" + ID + "/a\nb.xml",
        "relative/" + ID + "/a.xml",                // no leading slash
    };

    for (String path : paths) {
      // The raw URI check must not be the one that catches these, so the URI is kept neutral
      Result r = call(request("/bzox/view/x", path));
      assertEquals("path " + path, 400, r.status);
    }
  }

  /** A request with no path info at all (wrong mapping type) is refused, not served. */
  @Test
  public void rejectsMissingPathInfo() throws Exception {
    assertEquals(400, call(request("/bzox/view/a.xml", null)).status);
  }

  /** A path that is too long is rejected. */
  @Test
  public void rejectsOverlongPath() throws Exception {
    StringBuilder sb = new StringBuilder("/" + ID);
    while (sb.length() < 1100) sb.append("/aaaaaaaaaa");
    sb.append("/a.xml");

    assertEquals(400, get(sb.toString()).status);
  }

  // ---------------------------------------------------------------------------------------------
  // Extension allow list (fail-closed)
  // ---------------------------------------------------------------------------------------------

  /** Every extension of the default list is served. */
  @Test
  public void defaultListAllowsConfiguredExtensions() throws Exception {
    String[] allowed = {"zip", "xml", "json", "pdf", "csv", "xlsx", "docx", "txt", "html", "png", "jpg", "jpeg"};
    for (String ext : allowed) {
      writeFile(ID + "/f." + ext, "x".getBytes());
      assertEquals(ext, 200, get("/" + ID + "/f." + ext).status);
    }
  }

  /** Types outside the default list are refused with 403, even if the file exists. */
  @Test
  public void defaultListRefusesOtherExtensions() throws Exception {
    String[] refused = {"js", "css", "jsp", "class", "jar", "properties", "exe", "svg", "xsl"};
    for (String ext : refused) {
      writeFile(ID + "/f." + ext, "x".getBytes());
      Result r = get("/" + ID + "/f." + ext);
      assertEquals(ext, 403, r.status);
      assertEquals(ext, 0, r.body.length);
    }
  }

  /** Files without extension, or with a refused final extension, are refused. */
  @Test
  public void refusesMissingOrDoubleExtensions() throws Exception {
    writeFile(ID + "/noextension", "x".getBytes());
    writeFile(ID + "/a.xml.jsp", "x".getBytes());

    assertEquals(403, get("/" + ID + "/noextension").status);
    assertEquals(403, get("/" + ID + "/a.xml.jsp").status);
  }

  /** An explicit list replaces the default (this is what bzox-view does with .js/.css). */
  @Test
  public void explicitListReplacesDefault() throws Exception {
    init(".js, .CSS ,", null);
    writeFile(ID + "/a.js", "x".getBytes());
    writeFile(ID + "/a.css", "x".getBytes());
    writeFile(ID + "/a.xml", "x".getBytes());

    assertEquals(200, get("/" + ID + "/a.js").status);
    assertEquals(200, get("/" + ID + "/a.css").status);
    assertEquals(403, get("/" + ID + "/a.xml").status);
  }

  /** The correctly spelled parameter name is also read. */
  @Test
  public void readsCorrectlySpelledParameter() throws Exception {
    ServletConfig cfg = mock(ServletConfig.class);
    when(cfg.getInitParameter("extensions-allowed")).thenReturn(".txt");
    servlet.init(cfg);
    writeFile(ID + "/a.txt", "x".getBytes());
    writeFile(ID + "/a.xml", "x".getBytes());

    assertEquals(200, get("/" + ID + "/a.txt").status);
    assertEquals(403, get("/" + ID + "/a.xml").status);
  }

  /** Fail-closed: an explicit list that parses to nothing refuses everything. */
  @Test
  public void emptyExplicitListRefusesEverything() throws Exception {
    init(" , ,", null);
    writeFile(ID + "/a.xml", "x".getBytes());

    assertEquals(403, get("/" + ID + "/a.xml").status);
  }

  // ---------------------------------------------------------------------------------------------
  // File system containment
  // ---------------------------------------------------------------------------------------------

  /** Missing files give 404. */
  @Test
  public void returnsNotFoundForMissingFile() throws Exception {
    assertEquals(404, get("/" + ID + "/missing.xml").status);
  }

  /** A directory whose name looks like an allowed file gives 404, not a listing or an error. */
  @Test
  public void returnsNotFoundForDirectory() throws Exception {
    Files.createDirectories(base.toPath().resolve(ID).resolve("folder.xml"));

    assertEquals(404, get("/" + ID + "/folder.xml").status);
  }

  /** A symbolic link inside the folder that points outside it must not be followed. */
  @Test
  public void doesNotFollowSymlinksOutsideBase() throws Exception {
    File outside = tmp.newFolder("outside");
    Files.write(new File(outside, "secret.xml").toPath(), "secret".getBytes());
    Files.createDirectories(base.toPath().resolve(ID));
    Path link = base.toPath().resolve(ID).resolve("link");
    try {
      Files.createSymbolicLink(link, outside.toPath());
    } catch (UnsupportedOperationException | IOException | SecurityException e) {
      // Symbolic links are not available (for example Windows without privilege): nothing to test
      Assume.assumeNoException(e);
    }

    Result r = get("/" + ID + "/link/secret.xml");

    assertEquals(404, r.status);
    assertEquals(0, r.body.length);
  }

  /** A symbolic link to a file outside the folder is refused too. */
  @Test
  public void doesNotFollowFileSymlinksOutsideBase() throws Exception {
    File outsideFile = tmp.newFile("outside-secret.xml");
    Files.write(outsideFile.toPath(), "secret".getBytes());
    Files.createDirectories(base.toPath().resolve(ID));
    try {
      Files.createSymbolicLink(base.toPath().resolve(ID).resolve("leak.xml"), outsideFile.toPath());
    } catch (UnsupportedOperationException | IOException | SecurityException e) {
      Assume.assumeNoException(e);
    }

    assertEquals(404, get("/" + ID + "/leak.xml").status);
  }

  // ---------------------------------------------------------------------------------------------
  // HTML nonce rewrite
  // ---------------------------------------------------------------------------------------------

  /** Every script tag gets the nonce; other tags and tags that already have one are untouched. */
  @Test
  public void addsNonceToScriptTags() throws Exception {
    assumeHtmlMediaType();
    servlet.setNonceProvider(req -> NONCE);
    String html = "<html><script>a()</script><SCRIPT src=\"x.js\"></SCRIPT>"
        + "<scripts>no</scripts><script nonce=\"existing\">b()</script></html>";
    writeFile(ID + "/page.html", html.getBytes(StandardCharsets.UTF_8));

    Result r = get("/" + ID + "/page.html");
    String out = new String(r.body, StandardCharsets.UTF_8);

    assertEquals(200, r.status);
    assertTrue(out, out.contains("<script nonce=\"" + NONCE + "\">a()"));
    assertTrue(out, out.contains("<script nonce=\"" + NONCE + "\" src=\"x.js\">"));
    assertTrue(out, out.contains("<scripts>no</scripts>"));
    assertTrue(out, out.contains("<script nonce=\"existing\">b()"));
    assertEquals("nonce added exactly twice", 2, count(out, NONCE));
    assertEquals(r.body.length, r.contentLength);
  }

  /** Non-ASCII content survives the rewrite (UTF-8 in, UTF-8 out). */
  @Test
  public void keepsNonAsciiContentWhenRewriting() throws Exception {
    assumeHtmlMediaType();
    servlet.setNonceProvider(req -> NONCE);
    writeFile(ID + "/page.html", "<script>x</script> caf\u00e9 \u4e2d\u6587".getBytes(StandardCharsets.UTF_8));

    String out = new String(get("/" + ID + "/page.html").body, StandardCharsets.UTF_8);

    assertTrue(out, out.endsWith("caf\u00e9 \u4e2d\u6587"));
  }

  /** A nonce that could break out of the attribute is ignored: the file is served unchanged. */
  @Test
  public void ignoresUnsafeNonce() throws Exception {
    assumeHtmlMediaType();
    byte[] html = "<script>a()</script>".getBytes(StandardCharsets.UTF_8);
    writeFile(ID + "/page.html", html);
    String[] unsafe = {"abc\" onload=\"x()", "short", "has space 12345", "a$1b$2c$3d$4", "<b>12345678</b>"};

    for (String nonce : unsafe) {
      servlet.setNonceProvider(req -> nonce);
      Result r = get("/" + ID + "/page.html");
      assertEquals(nonce, 200, r.status);
      assertArrayEquals(nonce, html, r.body);
    }
  }

  /** Without a nonce the HTML is served unchanged. */
  @Test
  public void leavesHtmlUnchangedWithoutNonce() throws Exception {
    byte[] html = "<script>a()</script>".getBytes(StandardCharsets.UTF_8);
    writeFile(ID + "/page.html", html);

    assertArrayEquals(html, get("/" + ID + "/page.html").body);
  }

  /** HTML larger than the rewrite limit (5 MB) is streamed unchanged, protecting the heap. */
  @Test
  public void streamsLargeHtmlWithoutRewriting() throws Exception {
    assumeHtmlMediaType();
    servlet.setNonceProvider(req -> NONCE);
    byte[] big = new byte[5 * 1024 * 1024 + 1];
    java.util.Arrays.fill(big, (byte) ' ');
    byte[] start = "<script>a()</script>".getBytes(StandardCharsets.UTF_8);
    System.arraycopy(start, 0, big, 0, start.length);
    writeFile(ID + "/big.html", big);

    Result r = get("/" + ID + "/big.html");

    assertEquals(200, r.status);
    assertArrayEquals(big, r.body);
    assertEquals(big.length, r.contentLength);
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Skips (instead of failing) the HTML tests if the project's FileUtils does not report text/html
   * for .html files, since the media type comes from that library.
   */
  private void assumeHtmlMediaType() throws IOException {
    File probe = new File(base, "probe.html");
    Files.write(probe.toPath(), "<html></html>".getBytes());
    String type = org.pageseeder.ox.util.FileUtils.getMimeType(probe);
    Assume.assumeTrue("FileUtils returned " + type, type != null && type.startsWith("text/html"));
  }

  /**
   * Initialises the servlet with the given init parameters.
   *
   * @param extensions value of "extesions-allowed" (null = not set)
   * @param downloadable value of "downloadable" (null = not set)
   */
  private void init(String extensions, String downloadable) throws Exception {
    ServletConfig cfg = mock(ServletConfig.class);
    when(cfg.getInitParameter("extesions-allowed")).thenReturn(extensions);
    when(cfg.getInitParameter("downloadable")).thenReturn(downloadable);
    when(cfg.getServletName()).thenReturn("test-servlet");
    servlet.init(cfg);
  }

  /** Writes a file under the base folder, creating parent folders. */
  private void writeFile(String relative, byte[] content) throws IOException {
    Path file = base.toPath().resolve(relative);
    Files.createDirectories(file.getParent());
    Files.write(file, content);
  }

  /** Performs a GET whose raw URI is the view URI of the given path info. */
  private Result get(String pathInfo) throws Exception {
    return call(request("/bzox/view" + pathInfo, pathInfo));
  }

  /**
   * Creates a request mock.
   *
   * @param uri the raw request URI
   * @param pathInfo the decoded path info (may be null)
   * @param parameterNames names of query parameters present on the request
   */
  private HttpServletRequest request(String uri, String pathInfo, String... parameterNames) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn(uri);
    when(req.getPathInfo()).thenReturn(pathInfo);
    when(req.getParameterNames()).thenReturn(Collections.enumeration(java.util.Arrays.asList(parameterNames)));
    return req;
  }

  /** Runs doGet and captures what the servlet wrote to a fresh response. */
  private Result call(HttpServletRequest req) throws Exception {
    final Result result = new Result();
    HttpServletResponse res = mock(HttpServletResponse.class);
    when(res.getOutputStream()).thenReturn(new CapturingOutputStream(result.buffer));
    doAnswer(inv -> { result.status = (Integer) inv.getArguments()[0]; return null; }).when(res).setStatus(anyInt());
    doAnswer(inv -> { result.headers.put((String) inv.getArguments()[0], (String) inv.getArguments()[1]); return null; })
        .when(res).setHeader(anyString(), anyString());
    doAnswer(inv -> { result.contentType = (String) inv.getArguments()[0]; return null; }).when(res).setContentType(anyString());
    doAnswer(inv -> { result.contentLength = (Integer) inv.getArguments()[0]; return null; }).when(res).setContentLength(anyInt());
    doAnswer(inv -> { result.contentLength = (Long) inv.getArguments()[0]; return null; }).when(res).setContentLengthLong(anyLong());

    servlet.doGet(req, res);

    result.body = result.buffer.toByteArray();
    assertNotNull(result.headers);
    return result;
  }

  /** Counts the non-overlapping occurrences of a text. */
  private static int count(String text, String find) {
    int n = 0;
    for (int i = text.indexOf(find); i >= 0; i = text.indexOf(find, i + find.length())) n++;
    return n;
  }

  /** What the servlet wrote to the response. */
  private static final class Result {
    /** HTTP status; 200 unless the servlet called setStatus. */
    int status = 200;
    /** Headers set with setHeader. */
    final Map<String, String> headers = new HashMap<>();
    /** Content type set on the response. */
    String contentType;
    /** Content length set on the response (-1 if never set). */
    long contentLength = -1;
    /** Collects the response body while the servlet writes. */
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    /** The body bytes, filled after doGet returns. */
    byte[] body = new byte[0];
  }

  /** Minimal servlet output stream (Servlet 3.1) that stores everything in memory. */
  private static final class CapturingOutputStream extends ServletOutputStream {
    /** Destination of the written bytes. */
    private final ByteArrayOutputStream target;

    CapturingOutputStream(ByteArrayOutputStream target) {
      this.target = target;
    }

    @Override
    public void write(int b) {
      target.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
      target.write(b, off, len);
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener listener) {
      // Not used: the servlet writes synchronously
    }
  }
}