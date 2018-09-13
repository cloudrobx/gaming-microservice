<<<<<<< HEAD
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

=======
require('@google-cloud/trace-agent').start();
require('@google-cloud/debug-agent').start();
>>>>>>> d27c6e902bec88a70e076731ac9e9d61234693a9

'use strict';

// https://cloud.google.com/nodejs/docs/reference/datastore/1.4.x/Datastore#key

// [START app]
const express = require('express');
const app = express();
app.use(express.json());

const Datastore = require('@google-cloud/datastore');
//var config = require ('./config.js');
// Your Google Cloud Platform project ID
const projectId = 'gaming-microservice';

// Creates a client
const datastore = new Datastore({
  projectId: projectId,
});

// The kind for the new entity
const kind = 'User';
// The name/ID for the new entity
const name = 'abcde0';
// The Cloud Datastore key for the new entity
var userKey = datastore.key([kind, name]);

//var gcloud = require('google-cloud');
//var config = r'./config.js';
    
const ds = require('./datastore.js');
const bq = require('./bigquery.js');
const metric = require('./custom_metric.js');

const ENABLE_BIGQUERY_LOGGING = false;
const ENABLE_CUSTOM_METRIC = false;


app.get('/', (req, res) => {

	res.status(200).send("Hello from Nodejs App \n\n");

});

app.get('/get', (req, res) => {
	let start = Date.now();
	let username = req.query.user;
	userKey = datastore.key([kind, username]);
	ds.getUser(userKey, function (result){
		res.setHeader('content-type', 'application/json');
		res.status(200).send(result);
		let elapsed = Date.now() - start;
		
		// Log/Insert Internal Request Time
		console.log("metric:inner_time language:nodejs method:GET elapsed:" + elapsed + "ms");
		if(ENABLE_BIGQUERY_LOGGING) { bq.insertBQ("GET", elapsed); }
		if(ENABLE_CUSTOM_METRIC) { metric.createCustomMetric("GET", elapsed); }
	});
	

});


app.post('/put', (req, res) => {
	let start = Date.now();
	let username = req.query.user;
//	console.log(username);
	let payload = req.body;
//	console.log(payload);
	userKey = datastore.key([kind, username]);
	ds.addUser(userKey, payload, function (result){
		res.setHeader('content-type', 'application/json');
		res.status(200).send(result);
		let elapsed = Date.now() - start;
		
		// Log/Insert Internal Request Time
		console.log("metric:inner_time language:nodejs method:PUT elapsed:" + elapsed + "ms");
		if(ENABLE_BIGQUERY_LOGGING) { bq.insertBQ("PUT", elapsed); }
		if(ENABLE_CUSTOM_METRIC) { metric.createCustomMetric("PUT", elapsed); }
	});

	
});

app.post('/delete', (req, res) => {
	let username = req.query.user;
	let start = Date.now();
	userKey = datastore.key([kind, username]);
	ds.deleteUser(userKey, function (result){
		res.setHeader('content-type', 'application/json');
		res.status(200).send(result);
		let elapsed = Date.now() - start;

		// Log/Insert Internal Request Time
		console.log("metric:inner_time language:nodejs method:DELETE elapsed:" + elapsed + "ms");
		if(ENABLE_BIGQUERY_LOGGING) { bq.insertBQ("DELETE", elapsed); }
		if(ENABLE_CUSTOM_METRIC) { metric.createCustomMetric("DELETE", elapsed); }
	});

});


// Listen to the App Engine-specified port, or 8080 otherwise
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`Server listening on port ${PORT}...`);
});
// [END app]



module.exports = app;


