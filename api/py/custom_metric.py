import google
import sys

gae_dir = google.__path__.append('/usr/local/lib/python2.7/dist-packages')
sys.path.insert(0, gae_dir) # might not be necessary


from google.cloud import monitoring_v3
#
#import time
#
#def create_time_series_data(project_id, metric_value, metric_name, label_key, label_value):
#	client = monitoring_v3.MetricServiceClient()
#	project = project_id  # TODO: Update to your project ID.
#	project_name = client.project_path(project)
#	
#	series = monitoring_v3.types.TimeSeries()
#	series.metric.type = 'custom.googleapis.com/' + metric_name
#	series.resource.type = 'global'
#	series.resource.labels[label_key] = label_value
#	series.resource.labels['zone'] = 'us-central1-f'
#	point = series.points.add()
#	point.value.double_value = metric_value
#	now = time.time()
#	point.interval.end_time.seconds = int(now)
#	point.interval.end_time.nanos = int(
#	    (now - point.interval.end_time.seconds) * 10**9)
#	client.create_time_series(project_name, [series])
#	print('Successfully wrote time series.')