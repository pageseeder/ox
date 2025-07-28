/*
 * Copyright 2025 Allette Systems (Australia)
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
package org.pageseeder.ox.sql.step;

import org.pageseeder.ox.api.Result;
import org.pageseeder.ox.api.Step;
import org.pageseeder.ox.api.StepInfo;
import org.pageseeder.ox.core.Model;
import org.pageseeder.ox.core.PackageData;
import org.pageseeder.ox.core.ResultStatus;
import org.pageseeder.ox.sql.core.SqliteDBFactory;
import org.pageseeder.ox.tool.FileResultInfo;
import org.pageseeder.ox.tool.MultipleFilesResult;
import org.pageseeder.ox.util.StepUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A step to create a SQLite DB based on text files.
 *
 * <h3>Step Parameters</h3>
 * <ul>
 *  <li><var>input</var> can be a file or folder with the sql scripts. It also can be a commaseparated list
 *  of files or can have glob pattern.</li>
 *  <li><var>output</var> Where the database will be created. Like "sql/test.db" or "sql/test.sqlite"</li>
 * </ul>
 * @author Carlos Cabral
 * @since  13/03/2025
 */
public final class CreateSQLiteDB implements Step {

  private static Logger LOGGER = LoggerFactory.getLogger(CreateSQLiteDB.class);

  @Override
  public Result process(Model model, PackageData data, StepInfo info) {

    List<File> sqls = getInputFiles(data, info);
    File db = StepUtils.getOutput(data, info, null);
    List <FileResultInfo> fileResultInfos = new ArrayList<>();
    MultipleFilesResult<FileResultInfo> result = new MultipleFilesResult<>(model, data, info, db, fileResultInfos);
    /* Valid input : a folder */
    if (sqls == null || sqls.isEmpty()) {
      result.setError( new FileNotFoundException("Invalid input file"));
      LOGGER.error("Input is invalid.");
    } else if (db == null) {
      result.setError( new FileNotFoundException("Invalid output file"));
      LOGGER.error("Output is invalid.");
    } else {
      try {
//        db.mkdir();
        db.createNewFile();
        SqliteDBFactory dbFactory = new SqliteDBFactory(db);
        Connection conn = dbFactory.getConnection();
        for (File sql : sqls) {
          try {
            fileResultInfos.add(processFile(dbFactory, sql, db));
          } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            fileResultInfos.add(new FileResultInfo(sql, db, ResultStatus.ERROR));
          }
        }
        dbFactory.closeQuietly(conn);

      } catch (IOException ex) {
        LOGGER.error(ex.getMessage());
        result.setError( ex);
      }
    }
      //Create connection
      //Process each file and line
    return result;

  }

  private FileResultInfo processFile(SqliteDBFactory dbFactory, File sql, File db) throws SQLException {
    ResultStatus result = ResultStatus.OK;
    // Using BufferedReader
    try (BufferedReader br = Files.newBufferedReader(sql.toPath())) {
      String line;
      while ((line = br.readLine()) != null) {
        dbFactory.executeUpdate(line);
      }
    } catch (IOException | SQLException e) {
      LOGGER.error(e.getMessage(), e);
      result = ResultStatus.ERROR;
    } finally {
      dbFactory.getConnection().commit();
    }
    return new FileResultInfo(sql, db, result);
  }

  private List<File> getInputFiles(PackageData data,  StepInfo info) {
    String input = StepUtils.getParameter(data, info, "input", info.input());
    if (input == null || input.isEmpty()) {
      List<File> files = new ArrayList<>();
      //Upload file
      files.add(data.getOriginal());
      return files;
    }
    return data.getFiles(input);
  }
}

