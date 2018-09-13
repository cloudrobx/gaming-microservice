/*
 * Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * EDITING INSTRUCTIONS
 * This file is referenced in BigQuery's javadoc. Any change to this file should be reflected in
 * BigQuery's javadoc.
 */

package com.gamingmicroservice;

import com.google.api.client.util.Charsets;
import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQuery.DatasetListOption;
import com.google.cloud.bigquery.BigQuery.JobListOption;
import com.google.cloud.bigquery.BigQuery.TableDataListOption;
import com.google.cloud.bigquery.BigQuery.TableListOption;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobConfiguration;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.JobInfo.CreateDisposition;
import com.google.cloud.bigquery.JobStatistics.LoadStatistics;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDataWriteChannel;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.WriteChannelConfiguration;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import com.google.cloud.bigquery.BigQueryOptions;
 import java.sql.Timestamp;
import java.util.*;
import com.google.protobuf.util.Timestamps;
import java.text.SimpleDateFormat;

/**
 * This class contains a number of snippets for the {@link BigQuery} interface.
 * https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-examples/src/main/java/com/google/cloud/examples/bigquery/snippets
 */
public class BigQuerySnippets {

  private final BigQuery bigquery;

  public BigQuerySnippets() {
    this.bigquery = BigQueryOptions.getDefaultInstance().getService();
  }

  /**
   * Example of inserting rows into a table without running a load job.
   */
  // [TARGET insertAll(InsertAllRequest)]
  // [VARIABLE "my_dataset_name"]
  // [VARIABLE "my_table_name"]
  public InsertAllResponse insertAll(String method, long elapsed) {
    // [START bigquery_table_insert_rows]
    TableId tableId = TableId.of("demo_dataset", "gae_performance");
	
	// Get time
	Date currentTime = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String timestamp = sdf.format(currentTime); 
    System.out.println(timestamp);
    
    // Values of the row to insert
     Map<String, Object> rowContent = new HashMap<>();
	rowContent.put("time", timestamp);
    rowContent.put("language", "java");
    rowContent.put("method", method);
    rowContent.put("elapsed", elapsed);
    InsertAllResponse response =
        bigquery.insertAll(
            InsertAllRequest.newBuilder(tableId)
//                .addRow("rowId", rowContent)
				.addRow(rowContent)
                // More rows can be added in the same RPC by invoking .addRow() on the builder
                .build());
    if (response.hasErrors()) {
      // If any of the insertions failed, this lets you inspect the errors
      for (Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
        // inspect row error
        System.out.println(entry);
      }
    }
    // [END bigquery_table_insert_rows]
    return response;
  }



}