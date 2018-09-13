// Imports the Google Cloud client library
const monitoring = require('@google-cloud/monitoring');

// Your Google Cloud Platform project ID
const projectId = 'gaming-microservice';

// Creates a client
const client = new monitoring.MetricServiceClient();

function createCustomMetric(method, metric, callback) {
// Prepares an individual data point
const dataPoint = {
  interval: {
    endTime: {
      seconds: Date.now() / 1000,
    },
  },
  value: {
    // The amount of sales
    doubleValue: metric,
  },
};

// Prepares the time series request
const request = {
  name: client.projectPath(projectId),
  timeSeries: [
    {
      // Ties the data point to a custom metric
      metric: {
        type: 'custom.googleapis.com/global/app_internal_request',
        labels: {
          "http_method": method,
          "language" : "nodejs"
        },
      },
      resource: {
        type: 'global',
        labels: {
          project_id: projectId,
        },
      },
      points: [dataPoint],
    },
  ],
};

// Writes time series data
client
  .createTimeSeries(request)
  .then(results => {
    console.log(`Done writing time series data.`, results[0]);
//    return callback(results[0]); //if needed uncomment
  })
  .catch(err => {
    console.error('ERROR:', err);
//    return callback(err); // if needed 
  });
}

module.exports = {
	createCustomMetric: createCustomMetric
};