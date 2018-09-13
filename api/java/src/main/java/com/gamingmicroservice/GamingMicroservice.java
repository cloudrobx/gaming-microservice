// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// TODO: High-level file comment.

package com.gamingmicroservice;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.*;

import java.util.stream.*;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import com.google.cloud.bigquery.BigQuery;

// this is the only code-level dependency to GCP in this microservice
// if implemented with Mongo as a DB, there would be no GCO dependency
import com.google.cloud.datastore.*;

import com.google.cloud.bigquery.InsertAllResponse;
/** 
 * Gaming Microservice is an example of utilizing App Engine's serverless platform
 * to provide a gaming microservice for a user profile record.
 * App Engine provides the serverless web runtime for this microservice.
 * Google Cloud Datastore provides the serverless backing database.
 * 
 * The database schema in this example is extremely simple for each record
 * (called an Entity in Datastore).  It is structured as a key-value pair:
 * Key (name): the username
 * Value (data): the full JSON record for a user
 * 
 * We will also demonstrate a document-based schema in which each data element
 * is broken out into separate attributes with embedded arrays and objects
 * as needed (similar to how the data would be structured in Mongo).
 * 
 * This initial version demonstrates the serverless architecture of App Engine
 * and Datastore as well as the scaling properties of both.
 * 
 * 
 * Deploy in App Engine:
 *  mvn appengine:deploy -Dapp.deploy.version=your-version-name
 */
@WebServlet(name = "GamingMicroservice", urlPatterns = {"/get", "/put", "delete"})
public class GamingMicroservice extends HttpServlet {

  private Datastore datastore;
  private KeyFactory keyFactory;
  private Cache cache;

  private static final Logger logger = Logger.getLogger(GamingMicroservice.class.getName());
  private static CustomMetric metric;
  private static BigQuerySnippets bq;
  
  public static final boolean ENABLE_BIGQUERY_LOGGING = false;
  public static final boolean ENABLE_CUSTOM_METRIC = false;

  @Override
  public void init() {
    // initialize Google Cloud Datastore
    datastore = DatastoreOptions.getDefaultInstance().getService(); 
    keyFactory = datastore.newKeyFactory().setKind("User");      

    // initialize Dedicated Memcache
    try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
    } catch (CacheException e) {
      logger.log(Level.SEVERE,"Unable to initialize cache.",e);
    }
    
	if(ENABLE_BIGQUERY_LOGGING) {bq = new BigQuerySnippets();}
    if(ENABLE_CUSTOM_METRIC) {metric = new CustomMetric("gaming-microservice");}

    
  }

  /**
   * Handle adds or deletes.
   */  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String URI = request.getRequestURI();
    if ("/put".equals(URI)) {
      handlePut(request,response);
    }
    else if ("/delete".equals(URI))  {
      handleDelete(request,response);
    }
    else {
    }
  }
  
    /**
   * Get a user data record.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    long startTime = System.currentTimeMillis();
	
    String userName = request.getParameter("user");
    String jsonString;

    //jsonString = (String) cache.get(userName);

    // the schema in this example is a simple key-value pair
    // key = username
    // value = Json contents
    //if (jsonString == null) {
      Key entityKey = keyFactory.newKey(userName);
      Entity user = datastore.get(entityKey);
      if (user != null) {
        jsonString = new String(user.getBlob("data").toByteArray());
        //cache.put(userName, jsonString);
      }
      else {
        jsonString = "{\"message\":\"user not found\"}";
      }
   // }

    response.setContentType("application/json");
    response.getWriter().println(jsonString);
    long endTime = System.currentTimeMillis();
    long elapsed = endTime - startTime; 
     
     // Log/Insert Internal Request Time
    logger.info("metric:inner_time language:java method:GET elapsed:" + elapsed + "ms"); 
	if(ENABLE_BIGQUERY_LOGGING) { bq.insertAll("GET", elapsed); }
	if(ENABLE_CUSTOM_METRIC) { metric.createCustomMetric("GET", elapsed); }

  }

  /**
   * Put (add or update) a user data record.
   */
  public void handlePut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    long startTime = System.currentTimeMillis();

    String userName = request.getParameter("user");
    String jsonString = request.getReader().lines().collect(Collectors.joining());
    Key userKey = keyFactory.newKey(userName);
    Entity userEntity = Entity.newBuilder(userKey)  // Create the Entity
      .set("data", Blob.copyFrom(jsonString.getBytes()))           // Add Property ("author", book.getAuthor())
      .build(); 
    datastore.put(userEntity);
    //cache.put(userName, jsonString);

    response.setContentType("application/json");
    response.getWriter().println(userName);
    long endTime = System.currentTimeMillis();
    long elapsed = endTime - startTime;
	
	 // Log/Insert Internal Request Time
	logger.info("metric:inner_time language:java method:PUT elapsed:" + elapsed + "ms");
	if(ENABLE_BIGQUERY_LOGGING) { bq.insertAll("PUT", elapsed); }
	if(ENABLE_CUSTOM_METRIC) { metric.createCustomMetric("PUT", elapsed); }
  }

  /**
   * Delete a user data record. 
   */
  public void handleDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    long startTime = System.currentTimeMillis();

    String userName = request.getParameter("user");
    Key userKey = keyFactory.newKey(userName);
    datastore.delete(userKey);
    //cache.remove(userName);

    response.setContentType("application/json");
    response.getWriter().println(userName);
    long endTime = System.currentTimeMillis();
    long elapsed = endTime - startTime;
	
	 // Log/Insert Internal Request Time
	logger.info("metric:inner_time language:java method:DELETE elapsed:" + elapsed + "ms");
	if(ENABLE_BIGQUERY_LOGGING) { bq.insertAll("DELETE", elapsed); }
	if(ENABLE_CUSTOM_METRIC) { metric.createCustomMetric("DELETE", elapsed); } 
    
   }


  public static String getInfo() {
	return "Gaming Microservice";
  }

}
