#Gaming Microservice

This project is a demonstration of how to build a simple gaming-oriented microservice on App Engine, Google's serverless platform.

The project includes the following components:

* A simple "User Profile" service which provides an API endpoint to read and write gaming user profile info
* Multiple implementations of User Profile in different languages for comparison purposes (currently Python, Go, Java, and Node)
* A serverless database implemented in Cloud Datastore which contains tens of millions of randomly generated user records 
* A load tester utilizing Kubernetes, JMeter and Grafana to load test and demonstrate scaling and performance characteristics of different languages
* Logging data and dashboard for reviewing performance characteristics of different language versions

Possible future enhancements:
* Datastore conversion to Firestore
* Kubernetes based APIs (in addition to App Engine)
 
