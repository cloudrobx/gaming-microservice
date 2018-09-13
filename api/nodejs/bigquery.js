  // [START bigquery_table_insert_rows]
  // Imports the Google Cloud client library
  const BigQuery = require('@google-cloud/bigquery');

  /**
   * TODO(developer): Uncomment the following lines before running the sample.
   */
  const projectId = "gaming-microservice";
  const datasetId = "demo_dataset";
  const tableId = "gae_performance";
  
  // Creates a client
  const bigquery = new BigQuery({
    projectId: projectId,
  });

function insertBQ(method, elapsed, callback){
  var nowMilliseconds = new Date();
  const rows = [{
  	time: nowMilliseconds,
  	language: "nodejs",
  	method: method,
  	elapsed: elapsed
  }];

//  console.log(rows);
  
  // Inserts data into a table
  bigquery
    .dataset(datasetId)
    .table(tableId)
    .insert(rows)
    .then(() => {
      console.log(`Inserted ${rows.length} rows`);
//      return callback(`Inserted ${rows.length} rows`); //if needed uncomment
    })
    .catch(err => {
      if (err && err.name === 'PartialFailureError') {
      	 //      return callback(err); // if needed uncomment
        if (err.errors && err.errors.length > 0) {
          console.log('Insert errors:');
          err.errors.forEach(err => console.error(err));
          
        }
      } else {
        console.error('ERROR:', err);
 //      return callback(err); // if needed uncomment
      }
    });
}

module.exports = {
	insertBQ : insertBQ
};