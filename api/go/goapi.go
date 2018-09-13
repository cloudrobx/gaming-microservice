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

package main

import (
	
	//"io/ioutil"
	//"encoding/json"
	//"os"
	//	"log"
	"fmt"
	"net/http"
	"time"
	"cloud.google.com/go/datastore"
	"golang.org/x/net/context"
	"google.golang.org/appengine"
	"cloud.google.com/go/bigquery"
	"google.golang.org/appengine/log" //Info: https://cloud.google.com/appengine/docs/standard/go/logs/#Go_writing_application_logs
)



type visit struct {
        Timestamp time.Time
        UserIP    string
        ID        int64
}

// Item represents a row item.
type Item struct {
	Time     time.Time
	Language  string
	Method   string
	Elapsed  int64
}

type res struct {
	Data  []byte `datastore:"data"`
	Cache bool   `datastore:"-"`
}

type User struct {
	UserName      string      `json:"user_name"`
	VictoryRoyale int64 `json:"victory_royale"`
	Losses        int64 `json:"losses"`
	JoinDate      string `json:"join_date"`
	FavoriteGame  string `json:"favorite_game"`
	UserUUID      string `json:"user_uuid"`
}

var datastoreClient *datastore.Client
//var bqClient *bigquery.Client
var oneMillion int64
var projectID string
var datasetID string
var tableID string
const ENABLE_BIGQUERY_LOGGING bool = false
const ENABLE_CUSTOM_METRIC bool = false

func main() {

  oneMillion = int64(1000000)

  projectID = "gaming-microservice"
  datasetID = "demo_dataset"
  tableID   = "gae_performance"

  http.HandleFunc("/", mainHandler)
  http.HandleFunc("/put", gamePutHandler)
  http.HandleFunc("/get", gameGetHandler)
  http.HandleFunc("/delete", gameDeleteHandler)

  // start it up!!
  appengine.Main()
}


func insertRows(ctx context.Context, method string, elapsedTime int64) error {

	bqClient, _  := bigquery.NewClient(ctx, projectID)
	myDataset := bqClient.Dataset(datasetID)
	myTable   := myDataset.Table(tableID);

	u:= myTable.Uploader()

	items := []*Item{
		// Item implements the ValueSaver interface.
		{Time: time.Now(), Language: "go", Method: method, Elapsed: elapsedTime},
	}

	//fmt.Println("items to store are: ")
	//fmt.Println(items)
	if err := u.Put(ctx, items); err != nil {
		//fmt.Println("error updating rows")
		//fmt.Println(err)
		return err
	}
	// [END bigquery_table_insert_rows]
	return nil
}

func mainHandler(w http.ResponseWriter, r *http.Request) {

  fmt.Fprintf(w, "Hello from Go App!!!! \n")
}

func gameGetHandler(w http.ResponseWriter, r *http.Request) {

  start := time.Now()


  //fmt.Println("entered game get handler")
  ctx := appengine.NewContext(r)

  // Get the user from the url parameter
  user := r.URL.Query().Get("user")
  userData, err := getDSValues(w, ctx, user)

  if err != nil {
    //fmt.Println("some kind of get error")
    if err == datastore.ErrNoSuchEntity {
      //fmt.Println("no entitty error")
      http.Error(w, "", 200)
      return
    }
    http.Error(w, "", http.StatusInternalServerError)
    //log.Errorf(ctx, "DS GET Error:%v", err.Error())
    return
	}

	elapsed := time.Since(start)
  	elapsedNumber := int64(elapsed)
	elapsedNumberMs := int64(elapsedNumber/oneMillion)

	//fmt.Println("read this from datastore")
	//fmt.Println(*userData)
  
  // Log/Insert Internal Request Time
  log.Infof(ctx, "metric:inner_time language:go method:GET elapsed:%vms", elapsedNumberMs) 
  if ENABLE_BIGQUERY_LOGGING {insertRows(ctx, "GET", elapsedNumberMs)}
  if ENABLE_CUSTOM_METRIC { createCustomMetric(ctx, "GET", elapsedNumberMs) }

  fmt.Fprintf(w, "Got record:\n %s from datastore \n", userData)
  // Set the head so that any consumer can identify the content type
  //w.Header().Set("content-type", "application/json")
  //Write out values
  //w.Write(*userData)

}

func gamePutHandler(w http.ResponseWriter, r *http.Request) {

	   start := time.Now()

    //fmt.Println("just entered gamePutHandler. Request is: ")
    //fmt.Println(r.Body)

    ctx := appengine.NewContext(r)
    // Record this visit.
    if err := putDSValues(ctx, r); err != nil {
            msg := fmt.Sprintf("Could not save game data: %v", err)
            http.Error(w, msg, http.StatusInternalServerError)
            return
    }

    
    // some computation
    elapsed := time.Since(start)
    elapsedNumber := int64(elapsed)
    elapsedNumberMs := int64(elapsedNumber/oneMillion)
    
  	// Log/Insert Internal Request Time
    log.Infof(ctx, "metric:inner_time language:go method:PUT elapsed:%vms", elapsedNumberMs)
    if ENABLE_BIGQUERY_LOGGING {insertRows(ctx, "PUT", elapsedNumberMs)}
	if ENABLE_CUSTOM_METRIC { createCustomMetric(ctx, "PUT", elapsedNumberMs) }
	
	fmt.Fprintln(w, "Successfully stored an entry for the game. \n")
	//w.Header().Set("Content-Type", "text/plain; charset=utf-8") // normal header
	//w.WriteHeader(http.StatusOK)
	//w.Write("Successfully stored an entry for the game \n")

}


func gameDeleteHandler(w http.ResponseWriter, r *http.Request) {

    start := time.Now()

    //ctx := context.Background()
    ctx := appengine.NewContext(r)

    // Get the user from the url parameter
    user := r.URL.Query().Get("user")
    // Attempt to delete the value. Note there is no retry here so we should
    // return to the error to the caller
    err := deleteDSValues(ctx, user)
    if err != nil {
    	http.Error(w, "DS Error", 501)
    	//log.Errorf(ctx, "DS Delete Error:%v", err.Error())
    	return

  	}

  elapsed := time.Since(start)
  elapsedNumber := int64(elapsed)
  elapsedNumberMs := int64(elapsedNumber/oneMillion)
  
  // Log/Insert Internal Request Time
  log.Infof(ctx, "metric:inner_time language:go method:DELETE elapsed:%vms", elapsedNumberMs)
  if ENABLE_BIGQUERY_LOGGING {insertRows(ctx, "DELETE", elapsedNumberMs)}
  if ENABLE_CUSTOM_METRIC { createCustomMetric(ctx, "DELETE", elapsedNumberMs) }
  
  fmt.Fprintln(w, "Successfully deleted user \n")
  //w.Header().Set("Content-Type", "text/plain; charset=utf-8") // normal header
	//w.WriteHeader(http.StatusOK)
	//w.Write("Successfully deleted user \n")
}
