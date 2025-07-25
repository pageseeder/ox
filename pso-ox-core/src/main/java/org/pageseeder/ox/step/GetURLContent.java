package org.pageseeder.ox.step;

import org.pageseeder.ox.OXException;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.http.HttpErrorMessage;
import org.pageseeder.ox.http.HttpMethod;
import org.pageseeder.ox.http.Request;
import org.pageseeder.ox.http.Response;
import org.pageseeder.ox.tool.DefaultResult;
import org.pageseeder.ox.util.StepUtils;
import org.pageseeder.ox.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * The type Get url content.
 *
 * @author ccabral
 * @since 15 September 2021
 */
public class GetURLContent implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(GetURLContent.class);

  /**  It is maximum size allowed for each call of transfer form. */
  private static final long MAX_16MB = 1<<24;

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {
    LOGGER.debug("Start Find Pageseeder Projects or Group");

    String url = StepUtils.getParameter(data, info, "url", "");
    File output = StepUtils.getOutput(data, info, null);

    DefaultResult result = new DefaultResult(model, data, info, output);
    try {
      if (output != null) {
        output.getParentFile().mkdirs();
        output.createNewFile();
      }

      if (!StringUtils.isBlank(url)) {
        Response response = Request.response(HttpMethod.GET, url);
        if (response.isSuccessful()) {
          try(FileOutputStream fos = new FileOutputStream(output)) {
            response.consumeBytes(fos);
          }
        } else {
          result.setError(new OXException(new HttpErrorMessage(String.valueOf(response.code()), response.message())));
        }
      } else {
        result.setError(new OXException("URL cannot be blank"));
      }

    } catch (MalformedURLException e) {
      LOGGER.error(e.getMessage());
      result.setError(new OXException("Invalid URL: " + e.getMessage()));
    } catch (FileNotFoundException e) {
      LOGGER.error(e.getMessage());
      result.setError(new OXException("File Not Found Exception: " + e.getMessage()));
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      result.setError(new OXException("IO Exception: " + e.getMessage()));
//    } catch (InterruptedException e) {
//      e.printStackTrace();
    }
    return result;
  }
}
