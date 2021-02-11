package org.pageseeder.ox.tool;

import org.pageseeder.ox.util.StringUtils;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Represent some extra information for the result.
 *
 * @author ccabral
 * @since 18 January 2021
 */
public class ExtraResultInfo implements XMLWritable {

  /**
   * It is map of maps.
   * The idea to separate different information in different maps.
   * Example:
   * It will store the properties used to perform the step and the parameters
   * <p>
   * Then we create one map for each
   * <p>
   * extraInformation.put("properties", new Hash())
   * extraInformation.put("parameters", new Hash())
   * <p>
   * Let's add one value for each.
   * <p>
   * extraInformation.get("properties").put("number-of-pages", "15")
   * extraInformation.get("parameters").put("number-of-pages", "15")
   */
  private Map<String, Map<String, String>> extraInformation;


  /**
   * It is an extra xml that will be written to the result xml.
   * It must be valid.
   */
  private String extraXML;

  /**
   * Instantiates a new Extra result info.
   *
   * @param extraInformation the extra information
   */
  public ExtraResultInfo(Map<String, Map<String, String>> extraInformation) {
    this(extraInformation, null);
  }

  /**
   * Instantiates a new Extra result info.
   *
   * @param extraInformation the extra information
   */
  public ExtraResultInfo(Map<String, Map<String, String>> extraInformation, String extraXML) {
    this.extraInformation = extraInformation;
    this.extraXML = extraXML;
  }


  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("infos");
    if (this.extraInformation != null) {
      for (Map.Entry<String, Map<String, String>> infos: this.extraInformation.entrySet()) {
        String infoName = infos.getKey();
        for (Map.Entry<String, String> info: infos.getValue().entrySet()){
          xml.openElement(infoName);
          xml.attribute(info.getKey(), info.getValue());
          xml.closeElement();//InfoName
        }
      }
    }
    if (!StringUtils.isBlank(this.extraXML)) {
      xml.writeXML(this.extraXML);
    }
    xml.closeElement();//info
  }
}
