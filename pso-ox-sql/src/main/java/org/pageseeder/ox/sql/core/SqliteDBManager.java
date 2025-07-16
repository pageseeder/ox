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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * <p>This is a basic Database Manager for handle the sqlite database. </p>
 *
 * @author Carlos Cabral
 * @since 13 March 2025
 */
public final class SqliteDBManager {

  /** the logger */
  private final static Logger LOGGER = LoggerFactory.getLogger(SqliteDBManager.class);

  /** the connection */
  private final Connection _conn;

  /**
   * @param connection The connection
   */
  public SqliteDBManager(Connection connection) {
    this._conn = connection;
  }

  /**
   * To execute the select sql query.
   * @param sql the select sql query.
   * @return ResultSet
   */
  public ResultSet select(String sql) {
    try {
      Statement stat = this._conn.createStatement();
      ResultSet rs = stat.executeQuery(sql);
      return rs;
    } catch (SQLException ex) {
      LOGGER.error("Cannot execute the query {}", sql, ex);
    }
    return null;
  }

}
