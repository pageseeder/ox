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
package org.pageseeder.ox.psml.util;

import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.psml.validation.CharactersValidator;
import org.pageseeder.ox.psml.validation.ValidationResult;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The type Reports builder.
 */
public class ReportsBuilder {

  /**
   * Create psml report string.
   *
   * @param out     the out
   * @param results the results
   * @param status  the status
   * @return the string
   */
  public static String createPSMLReport(File out, Map<String, ValidationResult> results, ResultStatus status) {
    try {
      if (!out.getParentFile().exists()) out.getParentFile().mkdirs();
      if (out.exists() || out.createNewFile()) {
        try (FileOutputStream os = new FileOutputStream(out)) {
          XMLWriter html = new XMLWriterImpl(new OutputStreamWriter(os, StandardCharsets.UTF_8));
          html.openElement("document");
          html.attribute("level", "portable");
          html.writeXML("<section id='title1'><fragment id='title1'><heading level='2'>Validation report for "+
                              (results.size() != 1 ? results.size() + " documents" : results.keySet().iterator().next())+
                             "</heading><para><inline label='nb-documents'>"+results.size()+"</inline></para></fragment></section>");
          writeSummaryTable(html, results, status);
          writeExtra(html, results);
          writeResults(html, results, status);
          html.closeElement(); // document
          html.flush();
          html.close();
        }
      }
      return null;
    } catch (IOException ex) {
      return "Failed to create HTML report: "+ex.getMessage();
    }
  }

  private static void writeSummaryTable(XMLWriter psml, Map<String, ValidationResult> results, ResultStatus status) throws IOException {
    psml.openElement("section");
    psml.attribute("id", "summary");
    psml.openElement("fragment");
    psml.attribute("id", "summary");
    psml.openElement("table");
    if (results.size() == 1) {
      ValidationResult r = results.values().iterator().next();
      psml.writeXML("<row><hcell>Validated</hcell><cell role='boolean-"+(r.isValidated() ? "true" : "false")+"'></cell></row>");
      String text = r.hasErrors() ? r.errors().size()+" "+(status.toString().toLowerCase())+(r.errors().size() == 1 ? "" : "s")+" found" : "valid";
      psml.writeXML("<row><hcell>Status</hcell><cell role='validation-status-"+status.toString().toLowerCase()+"'>"+text+"</cell></row>");
    } else {
      int valid = 0;
      int invalid = 0;
      for (ValidationResult r : results.values()) {
        if (r.hasErrors()) invalid++;
        else valid++;
      }
      psml.writeXML("<row><cell>Total documents</cell><cell role='all-docs'>"+results.size()+"</cell></row>");
      psml.writeXML("<row><cell>Valid documents</cell><cell role='valid-docs'>"+valid+"</cell></row>");
      psml.writeXML("<row><cell>"+(status == ResultStatus.ERROR ? "Invalid document"+(invalid==1?"":"s") : "Document"+(invalid==1?"":"s")+" with warnings")+
                         "</cell><cell role='invalid-docs'>"+invalid+"</cell></row>");
    }
    psml.closeElement(); // table
    psml.closeElement(); // fragment
    psml.closeElement(); // section
  }

  private static void writeExtra(XMLWriter psml, Map<String, ValidationResult> results) throws IOException {
    if (results.size() == 1) {
      ValidationResult.ExtraData extra = results.values().iterator().next().extraData();
      if (extra != null && extra instanceof CharactersValidator.CharactersData) {
        CharactersValidator.charactersDataToPSML((CharactersValidator.CharactersData) extra, psml);
      }
    } else {
      Map<String, ValidationResult.ExtraData> extras = new HashMap<>();
      for (String path : results.keySet()) {
        ValidationResult vr = results.get(path);
        if (vr.extraData() != null) extras.put(path, vr.extraData());
      }
      if (!extras.isEmpty()) CharactersValidator.charactersDataToPSML(extras, psml);
    }
  }

  private static void writeResults(XMLWriter psml, Map<String, ValidationResult> results, ResultStatus status) throws IOException {
    if (results.size() == 1) {
      ValidationResult r = results.values().iterator().next();
      if (r.hasErrors()) {
        psml.openElement("section");
        psml.attribute("id", "details");
        psml.openElement("fragment");
        psml.attribute("id", "details");
        psml.writeXML("<heading level='3'>List of errors</heading>");
        psml.openElement("list");
        for (String err : r.errors()) {
          psml.element("item", err);
        }
        psml.closeElement(); // list
        psml.closeElement(); // fragment
        psml.closeElement(); // section
      }
    } else {
      psml.openElement("section");
      psml.attribute("id", "details");
      psml.openElement("fragment");
      psml.attribute("id", "details");
      psml.writeXML("<heading level='3'>List of documents</heading>");
      psml.openElement("table");
      List<String> sorted = new ArrayList<>(results.keySet());
      Collections.sort(sorted);
      for (String name : sorted) {
        ValidationResult r = results.get(name);
        String id = String.valueOf(r.hashCode());
        psml.openElement("row");
        if (r.hasErrors()) psml.attribute("role", "has-errors");
        psml.element("cell", name);
        psml.openElement("cell");
        psml.attribute("role", "validation-status-"+(r.hasErrors() ? status.toString().toLowerCase() : "ok"));
        if (!r.hasErrors()) {
          psml.writeText("Valid");
        } else {
          psml.writeXML("<link role='toggle-e"+id+"'>"+r.errors().size()+" "+(status.toString().toLowerCase())+(r.errors().size() == 1 ? "" : "s")+" found</link>");
          psml.openElement("list");
          psml.attribute("role", "toggle-e"+id);
          int count = 0;
          for (String err : r.errors()) {
            psml.element("item", err);
            if (count++ > 20) {
              psml.element("item", "Only first 20 errors shown...");
              break;
            }
          }
          psml.closeElement(); // list
        }
        psml.closeElement(); // cell
        psml.closeElement(); // row
      }
      psml.closeElement(); // table
      psml.closeElement(); // fragment
      psml.closeElement(); // section
    }
  }

  /**
   * Create the CSV report
   *
   * @param out     the out
   * @param results the results
   * @return the string
   */
  public static String createCSVReport(File out, Map<String, ValidationResult> results) {
    try {
      if (!out.getParentFile().exists()) out.getParentFile().mkdirs();
      if (out.exists() || out.createNewFile()) {
        try (FileOutputStream os = new FileOutputStream(out)) {
          if (results.size() == 1) {
            String input = results.keySet().iterator().next();
            ValidationResult r = results.get(input);
            writeLine(os, "input,validation,validated,valid");
            writeLine(os, "\""+input+"\","+
                r.name()+","+
                Boolean.toString(r.isValidated()).toLowerCase()+","+
                Boolean.toString(!r.hasErrors()).toLowerCase());
            writeLine(os, "");
            if (r.hasErrors()) {
              writeLine(os, "errors:");
              for (String err : r.errors()) {
                writeLine(os, "\""+err+"\"");
              }
            }
          } else {
            int valid = 0;
            int invalid = 0;
            for (ValidationResult r : results.values()) {
              if (r.hasErrors()) invalid++;
              else valid++;
            }
            writeLine(os, "total documents,"+results.size());
            writeLine(os, "valid documents,"+valid);
            writeLine(os, "invalid documents,"+invalid);
            writeLine(os, "");
            writeLine(os, "file, status");
            List<String> sorted = new ArrayList<>(results.keySet());
            Collections.sort(sorted);
            for (String input : sorted) {
              ValidationResult r = results.get(input);
              writeLine(os, "\""+input+"\","+(r.hasErrors() ? r.errors().size()+" errors found":"valid"));
              for (String err : r.errors()) {
                writeLine(os, ",\""+err+"\"");
              }
            }
          }
        }
      }
      return null;
    } catch (IOException ex) {
      return "Failed to create CSV report: "+ex.getMessage();
    }
  }

  private static void writeLine(OutputStream out, String s) throws IOException {
    out.write((s+"\n").getBytes(StandardCharsets.UTF_8));
  }
}
