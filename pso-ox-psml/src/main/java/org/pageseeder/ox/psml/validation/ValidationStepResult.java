package org.pageseeder.ox.psml.validation;

import org.pageseeder.ox.api.Downloadable;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.psml.util.ReportsBuilder;
import org.pageseeder.ox.tool.ResultBase;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ValidationStepResult extends ResultBase implements Downloadable {

  private Map<String, ValidationResult> _results = new HashMap<>();

  private final String _output;

  private String error = null;
  public ValidationStepResult(Model m, PackageData p, String output) {
    super(m, p);
    this._output = output;
  }

  public void addResults(String name, ValidationResult results) {
    this._results.put(name, results);
    if (results.hasErrors()) setStatus(ResultStatus.ERROR);
  }

  public void finished() {
    if (this._output != null && this._output.endsWith(".psml"))
      this.error = ReportsBuilder.createPSMLReport(downloadPath(), this._results, this.status());
    if (this._output != null && this._output.endsWith(".csv"))
      this.error = ReportsBuilder.createCSVReport(downloadPath(), this._results);
    done();
  }

  @Override
  public boolean isDownloadable() {
    return this._output != null && error == null;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("result");
    xml.attribute("id", data().id());
    xml.attribute("model", model().name());
    xml.attribute("status", status().toString().toLowerCase());
    xml.attribute("time", Long.toString(time()));
    xml.attribute("downloadable", isDownloadable() ? "true" : "false");
    if (this._output != null)      xml.attribute("path", data().getPath(downloadPath()));
    if (this._results.size() == 1) xml.attribute("input", this._results.keySet().iterator().next());
    if (this.error != null)        xml.attribute("error", this.error);
    if (this._results != null) {
      // add  infos for the UI
      xml.openElement("infos");
      xml.attribute("name", "validation");
      if (this._results.size() == 1) {
        ValidationResult r = this._results.values().iterator().next();
        xml.openElement("info");
        xml.attribute("name", "validated");
        xml.attribute("value", r.isValidated() ? "true" : "false");
        xml.attribute("type", "string");
        xml.closeElement();
        xml.openElement("info");
        xml.attribute("name", "valid");
        xml.attribute("value", r.hasErrors() ? "false" : "true");
        xml.attribute("type", "string");
        xml.closeElement();
        if (r.hasErrors()) {
          xml.openElement("info");
          xml.attribute("name", "number errors");
          xml.attribute("value", r.errors().size());
          xml.attribute("type", "string");
          xml.closeElement();
        }
      } else {
        int valid = 0;
        int invalid = 0;
        for (ValidationResult r : this._results.values()) {
          if (r.hasErrors()) invalid++;
          else valid++;
        }
        xml.openElement("info");
        xml.attribute("name", "total documents");
        xml.attribute("value", this._results.size());
        xml.attribute("type", "string");
        xml.closeElement();
        xml.openElement("info");
        xml.attribute("name", "valid documents");
        xml.attribute("value", String.valueOf(valid));
        xml.attribute("type", "string");
        xml.closeElement();
        xml.openElement("info");
        xml.attribute("name", "invalid documents");
        xml.attribute("value", String.valueOf(invalid));
        xml.attribute("type", "string");
        xml.closeElement();
      }
      xml.closeElement(); // infos
    }
    xml.closeElement(); // result
  }

  @Override
  public File downloadPath() {
    return data().getFile(this._output);
  }
}
