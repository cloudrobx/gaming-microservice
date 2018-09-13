package com.gamingmicroservice;

import java.io.IOException;
import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.utils.SystemProperty;

/**
 * 
 * Adding Stackdriver customer metrics as attempt
 */
public class CustomMetric {
	
	private String projectId;
	
	public CustomMetric(String projectId) {
        this.projectId = projectId;
    }
    
  	void createCustomMetric(String method, long elapsed) throws IOException {
	  	// Google Cloud Platform project ID
	    String projectId = this.projectId;
	
	    if (projectId == null) {
	      System.err.println("Project ID is Null");
	      return;
	    }
	
	    // Instantiates a client
	    MetricServiceClient metricServiceClient = MetricServiceClient.create();
	
	    // Prepares an individual data point
	    TimeInterval interval = TimeInterval.newBuilder()
	        .setEndTime(Timestamps.fromMillis(System.currentTimeMillis()))
	        .build();
	    TypedValue value = TypedValue.newBuilder()
	        .setDoubleValue(elapsed)
	        .build();
	    Point point = Point.newBuilder()
	        .setInterval(interval)
	        .setValue(value)
	        .build();
	
	    List<Point> pointList = new ArrayList<>();
	    pointList.add(point);
	
	    ProjectName name = ProjectName.of(projectId);
	
	    // Prepares the metric descriptor
	    Map<String, String> metricLabels = new HashMap<String, String>();
	    metricLabels.put("http_method", method);
	   	metricLabels.put("language", "java");
	    Metric metric = Metric.newBuilder()
	        .setType("custom.googleapis.com/global/app_internal_request") // "global/name_of_metric"
	        .putAllLabels(metricLabels)
	        .build();
	
	    // Prepares the monitored resource descriptor
	    Map<String, String> resourceLabels = new HashMap<String, String>();
	    resourceLabels.put("project_id", projectId);
	    MonitoredResource resource = MonitoredResource.newBuilder()
	        .setType("global")
	        .putAllLabels(resourceLabels)
	        .build();
	
	    // Prepares the time series request
	    TimeSeries timeSeries = TimeSeries.newBuilder()
	        .setMetric(metric)
	        .setResource(resource)
	        .addAllPoints(pointList)
	        .build();
	    List<TimeSeries> timeSeriesList = new ArrayList<>();
	    timeSeriesList.add(timeSeries);
	
	    CreateTimeSeriesRequest request = CreateTimeSeriesRequest.newBuilder()
	        .setName(name.toString())
	        .addAllTimeSeries(timeSeriesList)
	        .build();
	
	    // Writes time series data
	    metricServiceClient.createTimeSeries(request);
	
	    System.out.printf("Done writing time series data.%n");
	
	    metricServiceClient.close();
  	}
  	
}