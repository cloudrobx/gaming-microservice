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
    //"os"
	//"time"
	//"io/ioutil"
	//"google.golang.org/appengine/memcache"
	//"google.golang.org/appengine"
	//"cloud.google.com/go/bigquery"
	"fmt"
	"log"
	"net/http"
	"encoding/json"
	"cloud.google.com/go/datastore"
	"golang.org/x/net/context"

)

func putDSValues(ctx context.Context, r *http.Request) error {

	//fmt.Println("just entered putDSValues")

	decoder := json.NewDecoder(r.Body)
    var tv User
    err := decoder.Decode(&tv)
    if err != nil {
        panic(err)
    }

	//fmt.Println("tv username is: ")
	//fmt.Println(tv.UserUUID)

	//var err error
	datastoreClientLocal, err := datastore.NewClient(ctx, projectID)

    if err != nil {
            log.Fatal(err)
    }


	myInterface := &User {
	UserName: tv.UserName,
	VictoryRoyale: tv.VictoryRoyale,
	Losses:        tv.Losses,
	JoinDate:      tv.JoinDate,
	FavoriteGame:  tv.FavoriteGame,
	UserUUID:      tv.UserUUID,
	}

	// let put the orginal in datastore
	k := datastore.NameKey("GoUser", tv.UserName, nil)
	if _, err := datastoreClientLocal.Put(ctx, k, myInterface); err != nil {
		//fmt.Println("error putting data in DS store client")
		return err
	}

	//fmt.Println("just stored game data")
	return nil
}



func deleteDSValues(ctx context.Context, user string) error {

	//var err error
	datastoreClientLocal, err := datastore.NewClient(ctx, projectID)
        if err != nil {
                log.Fatal(err)
        }

	// Build a key
	k := datastore.NameKey("GoUser", user, nil)
	// Delete the DS entry
	if err := datastoreClientLocal.Delete(ctx, k); err != nil {
		return err
	}
	return nil
}



func getDSValues(w http.ResponseWriter, ctx context.Context, user string) (*User, error) {

//	// Lets try to get it from memcache first.  If it fails  we will go to datastore.
//	item, err := memcache.Get(ctx, user)
//	// If memcache has it let return and we are done.
//	if err == nil {
//		return &item.Value, nil
//	}

	// Ok no memcache support so lets go to datastore
	// there is not retry pattern here so we would only
	// be as good as datastore here.

	datastoreClientLocal, err := datastore.NewClient(ctx, projectID)
        if err != nil {

                log.Fatal(err)
        }


	k := datastore.NameKey("GoUser", user, nil)
	 //fmt.Println(user)

	thisUser := &User{}
	// on error return it
	if err := datastoreClientLocal.Get(ctx, k, thisUser); err != nil {
		fmt.Fprintf(w, "Got error")
		fmt.Fprintf(w, err.Error())
		return nil, err
	}
	
//	// We got it so load it to memcache
//	// this should level thing out and drop respnse times for the same item.
//	item = &memcache.Item{
//		Key:   user,
//		Value: e.Data,
//	}
//
//	// Log the any memcache set error.
//	if err := memcache.Set(ctx, item); err != nil {
//		log.Errorf(ctx, "Memcache error:%v", err.Error())
//	}

    users := make([]*User, 1)
    users[0] = thisUser

	return users[0], nil
}
