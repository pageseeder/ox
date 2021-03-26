package org.pageseeder.ox.psml.validation;

import org.pageseeder.xmlwriter.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharactersValidator {

  /**
   * Validate well formed XML
   *
   * @param original the file to validate
   *
   * @return the result of the validation, containing errors and if it was actually validated
   */
  public ValidationResult validateCharacters(File original) {
    String error;
    try (InputStream reader = new FileInputStream(original)) {
      CharactersData data = new CharactersData();
      List<String> errors = new ArrayList<>();
      int linenb = 1;
      int colnb = 1;
      byte[] buffer = new byte[1024 * 4];
      int read;
      while ((read = reader.read(buffer)) != -1) {
        String s = new String(buffer, 0, read, StandardCharsets.UTF_8);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c == '\n') {
            linenb++;
            colnb = 0;
          } else if (invalidChar(c)) {
            data.addChar(c);
            errors.add("Suspicious character '"+c+"' found at line "+linenb+", character "+colnb+". It can be replaced by entity &#x"+Integer.toHexString(c)+";");
          }
          colnb++;
        }
      }
      return new ValidationResult("characters", true, null, errors, data);
    } catch (IOException ex) {
      error = "Error when inspecting file: " + ex.getMessage();
    }
    return new ValidationResult("characters", true, null, error);
  }

  private boolean invalidChar(char c) {
    return Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN;
  }

  public static class CharactersData implements ValidationResult.ExtraData {
    private Map<Character, Integer> characters = new HashMap<>();
    void addChar(char c) {
      Integer v = this.characters.get(c);
      if (v == null) this.characters.put(c, 1);
      else this.characters.put(c, v.intValue() + 1);
    }
    @Override
    public void toXML(XMLWriter xml) throws IOException {
      xml.openElement("characters");
      for (Character c : this.characters.keySet()) {
        xml.openElement("character");
        xml.attribute("value", c);
        xml.attribute("occurrence", this.characters.get(c));
        xml.closeElement();
      }
      xml.closeElement();
    }
  }

  public static void charactersDataToPSML(CharactersData data, XMLWriter psml) throws IOException {
    if (data == null || data.characters.isEmpty()) return;
    psml.openElement("section");
    psml.attribute("id", "extra");
    psml.openElement("fragment");
    psml.attribute("id", "extra");
    psml.openElement("table");
    psml.writeXML("<row part='header'><hcell>Character</hcell><hcell>Entity</hcell><hcell>Occurrences</hcell></row>");
    for (Character c : data.characters.keySet()) {
      psml.openElement("row");
      psml.element("hcell", c.toString());
      psml.element("cell", "&#x"+Integer.toHexString(c)+";");
      psml.element("cell", data.characters.get(c).toString());
      psml.closeElement();
    }
    psml.closeElement(); // table
    psml.closeElement(); // fragment
    psml.closeElement(); // section
  }

  public static void charactersDataToPSML(Map<String, ValidationResult.ExtraData> datas, XMLWriter psml) throws IOException {
    if (datas == null || datas.isEmpty()) return;
    Map<Character, CharacterData> characters = new HashMap<>();
    for (String path : datas.keySet()) {
      ValidationResult.ExtraData data = datas.get(path);
      if (data instanceof CharactersData) {
        boolean newdoc = true;
        for (Character c : ((CharactersData) data).characters.keySet()) {
          CharacterData cdata = characters.get(c);
          if (cdata == null) characters.put(c, new CharacterData(path));
          else {
            if (newdoc) cdata.files.add(path);
            cdata.occurrence++;
          }
          newdoc = false;
        }
      }
    }
    if (characters.isEmpty()) return;
    psml.openElement("section");
    psml.attribute("id", "extra");
    psml.openElement("fragment");
    psml.attribute("id", "extra");
    psml.openElement("table");
    psml.writeXML("<col width='20%' /><col width='20%' /><col />");
    psml.writeXML("<row part='header'><hcell align='center'>Character</hcell><hcell align='center'>Entity</hcell><hcell>Occurrences</hcell></row>");
    for (Character c : characters.keySet()) {
      String id = String.valueOf(c.hashCode());
      CharacterData cdata = characters.get(c);
      psml.openElement("row");
      psml.element("hcell", c.toString());
      psml.element("hcell", "&#x"+Integer.toHexString(c)+";");
      psml.openElement("cell");
      psml.writeXML("<link role='toggle-c"+id+"'>"+cdata.occurrence+" in "+cdata.files.size()+" document"+(cdata.files.size()==1?"":"s")+"</link>");
      psml.openElement("list");
      psml.attribute("role", "toggle-c"+id);
      int count = 0;
      for (String p : cdata.files) {
        psml.element("item", p);
        if (count++ > 20) {
          psml.element("item", "Only first 20 documents shown...");
          break;
        }
      }
      psml.closeElement(); // list
      psml.closeElement(); // cell
      psml.closeElement(); // row
    }
    psml.closeElement(); // table
    psml.closeElement(); // fragment
    psml.closeElement(); // section
  }

  private static class CharacterData {
    int occurrence = 1;
    List<String> files = new ArrayList<>();
    CharacterData(String p) { this.files.add(p); }
  }
}
