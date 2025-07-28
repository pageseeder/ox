package org.pageseeder.ox.berlioz.util;

import org.pageseeder.berlioz.GlobalSettings;
import org.pageseeder.berlioz.content.ContentRequest;
import org.pageseeder.berlioz.servlet.HttpContentRequest;
import org.pageseeder.ox.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ccabral
 * @since 06 June 2024
 */
public class NonceUtils {

  /**
   * Nonce is a unique code per request which is placed in the HTTP response headers and all javascript and/or css files
   * an code should have a reference to it.
   * @param req the request
   * @return a String
   */
  public static String getNonce(ContentRequest req) {
    return req instanceof HttpContentRequest ? getNonce((HttpContentRequest)  req) : null;
  }

  /**
   * Nonce is a unique code per request which is placed in the HTTP responde headers and all javascript and/or css files
   * an code should have a reference to it.
   * @param req the request
   * @return a String
   */
  public static String getNonce(HttpContentRequest req) {
    return getNonce(req.getHttpRequest());
  }


  /**
   * Nonce is a unique code per request which is placed in the HTTP response headers and all javascript and/or css files
   * an code should have a reference to it.
   * @param req the request
   * @return a String
   */
  public static String getNonce(HttpServletRequest req) {
    String nonce = null;
    //Check if nonce is active
    boolean nonceActive = "true".equalsIgnoreCase(GlobalSettings.get("berlioz.nonce.enable"));
    if (nonceActive) {
      String nonceAttributeName = GlobalSettings.get("berlioz.nonce.attribute");
      if (!StringUtils.isBlank(nonceAttributeName)) {
        nonce = (String) req.getAttribute(nonceAttributeName);
      }
    }
    return nonce;
  }
}
