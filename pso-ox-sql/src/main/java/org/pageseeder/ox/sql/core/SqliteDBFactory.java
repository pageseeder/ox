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
package org.pageseeder.ox.sql.core;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory class to deal with sqlite db.
 *
 * @author Carlos Cabral
 * @since 13/03/2025
 */
public class SqliteDBFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqliteDBFactory.class);

  private static final String DATABASE_DRIVERNAME = "org.sqlite.JDBC";

  private final File _db;

  private Connection dbconn = null;

  /**
   * @param db the SQLite file.
   */
  public SqliteDBFactory(File db) {
    if (db == null || db.isDirectory()) {
      throw new IllegalArgumentException("DB root must be a file." + db);
    }
    if (!db.exists()) {
      throw new IllegalArgumentException("Cannot find the sqlite file: " + db);
    }
    this._db = db;
  }

  /***
   * Return the database connection.
   *
   * @return the connection object.
   */
  public Connection getConnection() {
    if (this.dbconn == null) {
      configure();
    }
    return this.dbconn;
  }

  /**
   * @param connection the database connection to close.
   */
  public void closeQuietly(Connection connection) {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException ex) {
      LOGGER.warn("Unable to close connection", ex);
    }
  }

  /**
   * @param sql the sql script to execute.
   * @throws SQLException
   */
  public void executeUpdate(String sql) throws SQLException {
    if (this.dbconn == null) {
      configure();
    }
    Statement statement = this.dbconn.createStatement();
    LOGGER.debug("sql {} ", sql);
    int no = statement.executeUpdate(sql);
    LOGGER.debug("number of execute statement {} ", no);
  }

  /**
   * config the database connection.
   */
  private void configure() {
    try {
      Class.forName(DATABASE_DRIVERNAME);
      this.dbconn = DriverManager.getConnection("jdbc:sqlite:" + this._db.getAbsolutePath());
      this.dbconn.setAutoCommit(false);
    } catch (ClassNotFoundException ex) {
      LOGGER.error("Cannot find the sqlite JDBC connector.", ex);
    } catch (SQLException ex) {
      LOGGER.error("SQL exception", ex);
    }
  }
}
