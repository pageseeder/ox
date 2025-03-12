package org.pageseeder.ox.sql.step;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pageseeder.ox.OXConfig;
import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.StepInfoImpl;
import org.pageseeder.ox.step.Compression;
import org.pageseeder.ox.step.Copy;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ccabral
 * @since 13 March 2025
 */
public class TestCreateSQLiteDB {

  @Test
  public void test() {
    File sqlsDirectory = new File("src/test/resources/org/pageseeder/ox/sql/create-db/sqls/customers.sql");
    Model model = new Model("test");
    PackageData data = PackageData.newPackageData("sql-lite", sqlsDirectory);
    Map<String, String> params = new HashMap<>();

    StepInfoImpl info = new StepInfoImpl("step-id", "step name", "customers.sql", "test.db", params);

    try {
      CreateSQLiteDB step = new CreateSQLiteDB();
      Result result = step.process(model, data, info);
      XMLStringWriter writer = new XMLStringWriter(XML.NamespaceAware.No);
      result.toXML(writer);
      System.out.println(writer.toString());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertTrue(data.getFile("test.db").exists());
  }
}
