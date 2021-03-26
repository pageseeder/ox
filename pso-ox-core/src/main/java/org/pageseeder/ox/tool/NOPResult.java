/*
 * Copyright (c) 1999-2016 Allette systems pty. ltd.
 */
package org.pageseeder.ox.tool;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;

/**
 * A implementation of {@link Result} which is to indicate no operation of the result.
 *
 * <h3>Return XML</h3>
 * <pre>{@code
 *   <result type="no-operation" downloadable="false"/>
 * }</pre>
 *
 * @author Ciber Cai
 * @since 18 July 2016
 */
public class NOPResult extends ResultBase implements Result {

  public NOPResult(Model model, PackageData data) {
    super(model, data);
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("result");
    xml.attribute("type", "no-operation");
    xml.attribute("downloadable", String.valueOf(isDownloadable()));
    xml.closeElement();
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }
}
