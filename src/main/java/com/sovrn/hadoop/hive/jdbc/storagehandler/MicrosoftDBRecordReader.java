/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sovrn.hadoop.hive.jdbc.storagehandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.lib.db.DBInputFormat;
import org.apache.hadoop.mapreduce.lib.db.DBRecordReader;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;

import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A RecordReader that reads records from an Oracle SQL table.
 */
public class MicrosoftDBRecordReader<T extends DBWritable> extends DBRecordReader<T> {

  /** Configuration key to set to a timezone string. */
  //public static final String SESSION_TIMEZONE_KEY = "oracle.sessionTimeZone";

  private static final Log LOG = LogFactory.getLog(MicrosoftDBRecordReader.class);
  private DBInputFormat.DBInputSplit split;

  public MicrosoftDBRecordReader(DBInputFormat.DBInputSplit split, 
      Class<T> inputClass, Configuration conf, Connection conn, DBConfiguration dbConfig,
      String cond, String [] fields, String table) throws SQLException {
    super(split, inputClass, conf, conn, dbConfig, cond, fields, table);
    split = getSplit();
    //setSessionTimeZone(conf, conn);
  }

  /** Returns the query for selecting the records from an Oracle DB. */
  protected String getSelectQuery() {
    StringBuilder query = new StringBuilder();
    DBConfiguration dbConf = getDBConf();
    String conditions = getConditions();
    String tableName = getTableName();
    String [] fieldNames = getFieldNames();

    // Oracle-specific codepath to use rownum instead of LIMIT/OFFSET.
    if(dbConf.getInputQuery() == null) {
      query.append("SELECT ");
  
      for (int i = 0; i < fieldNames.length; i++) {
        query.append(fieldNames[i]);
        if (i != fieldNames.length -1) {
          query.append(", ");
        }
      }
  
      query.append(" FROM ").append(tableName);
      if (conditions != null && conditions.length() > 0)
        query.append(" WHERE ").append(conditions);
      String orderBy = dbConf.getInputOrderBy();
      if (orderBy != null && orderBy.length() > 0) {
        query.append(" ORDER BY ").append(orderBy);
      }

    } else {
      //PREBUILT QUERY
      query.append(dbConf.getInputQuery());
    }

    try {
      query.append(" OFFSET ").append(split.getStart());
      query.append(" FETCH NEXT ").append(split.getLength()).append(" ROWS ONLY ");
    } catch (Exception ex) {
      // Ignore, will not throw.
    }   
    return query.toString();
  }

  
}
