package main

import (
	  "fmt"
        "log"
        "time"

        // Imports the Stackdriver Monitoring client package.
        monitoring "cloud.google.com/go/monitoring/apiv3"
        googlepb "github.com/golang/protobuf/ptypes/timestamp"
        "golang.org/x/net/context"
        metricpb "google.golang.org/genproto/googleapis/api/metric"
        monitoredrespb "google.golang.org/genproto/googleapis/api/monitoredres"
        monitoringpb "google.golang.org/genproto/googleapis/monitoring/v3"
)

func createCustomMetric(ctx context.Context, method string, metric_value int64) {

        // Creates a client.
        client, err := monitoring.NewMetricClient(ctx)
        if err != nil {
                log.Fatalf("Failed to create client: %v", err)
        }

        // Sets your Google Cloud Platform project ID.
        projectID := "gaming-microservice"

        // Prepares an individual data point
        dataPoint := &monitoringpb.Point{
                Interval: &monitoringpb.TimeInterval{
                        EndTime: &googlepb.Timestamp{
                                Seconds: time.Now().Unix(),
                        },
                },
                Value: &monitoringpb.TypedValue{
                        Value: &monitoringpb.TypedValue_DoubleValue{
                                DoubleValue: float64(metric_value),
                        },
                },
        }

        // Writes time series data.
        if err := client.CreateTimeSeries(ctx, &monitoringpb.CreateTimeSeriesRequest{
                Name: monitoring.MetricProjectPath(projectID),
                TimeSeries: []*monitoringpb.TimeSeries{
                        {
                                Metric: &metricpb.Metric{
                                        Type: "custom.googleapis.com/global/app_internal_request",
                                        Labels: map[string]string{
                                                "http_method": method,
                                                "language" : "go",
                                        },
                                },
                                Resource: &monitoredrespb.MonitoredResource{
                                        Type: "global",
                                },
                                Points: []*monitoringpb.Point{
                                        dataPoint,
                                },
                        },
                },
        }); err != nil {
                log.Fatalf("Failed to write time series data: %v", err)
        }

        // Closes the client and flushes the data to Stackdriver.
        if err := client.Close(); err != nil {
                log.Fatalf("Failed to close client: %v", err)
        }

        fmt.Printf("Done writing time series data.\n")
}