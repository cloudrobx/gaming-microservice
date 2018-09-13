import datetime
import pytz

# https://google-cloud-python.readthedocs.io/en/latest/bigquery/usage.html
from google.cloud import bigquery

def bq_insert_rows(method, elapsed):
	client = bigquery.Client()
	dataset_ref = client.dataset('demo_dataset')
	table_ref = dataset_ref.table('gae_performance')
	table = client.get_table(table_ref)  # API call
	rows_to_insert = [
	    (datetime.datetime.now(pytz.utc), 'python', method, elapsed)
	]
	errors = client.insert_rows(table, rows_to_insert)  # API request
	
	assert errors == []