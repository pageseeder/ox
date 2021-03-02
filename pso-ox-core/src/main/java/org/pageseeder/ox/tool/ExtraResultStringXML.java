package org.pageseeder.ox.tool;

import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

/**
 * Represent some extra information for the result.
 *
 * @author ccabral
 * @since 18 January 2021
 */
public class ExtraResultStringXML implements XMLWritable {

  /**
   * It is an extra xml that will be written to the result xml.
   * It must be valid.
   */
  private String extraXML;

   /**
   * Instantiates a new Extra result info.
   *
   * @param extraXML the extra information
   */
  public ExtraResultStringXML(String extraXML) {
    this.extraXML = extraXML;
  }


  @Override
  public void toXML(XMLWriter xml) throws IOException {
    if (!StringUtils.isBlank(this.extraXML)) {
      xml.writeXML(this.extraXML);
    }
  }
}
