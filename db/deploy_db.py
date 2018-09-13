#!/usr/bin/python
#
# Copyright 2018 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""TODO: High-level file comment."""

import sys


def main(argv):
    pass


if __name__ == '__main__':
    main(sys.argv)

from google.cloud import datastore
import json
import sys
from threading import Thread
from queue import Queue
import uuid
from datetime import datetime
import random
from itertools import permutations
from itertools import product

project_id = 'gaming-microservice'
threads = int(sys.argv[1])

# callback for workers to save a chunk of users
def saver(i, datastore_client, users):
  datastore_client.put_multi(users)
  print str(i) + ', ',
  sys.stdout.flush()
  
datastore_client = datastore.Client(project=project_id)
kind = 'User'

# truncate the table
query = datastore_client.query(kind='User')
keys = query.keys_only()
print 'Deleting' 
datastore_client.delete_multi(keys)
print 'Finished deleting.' 


users = []

# worker loop for a single thread
def worker():
    while True:
      if q is not None:
        item = q.get()
        if item is not None:
          i = item.get('i')
          users = item.get('users')
	  saver(i, datastore_client, users)
        q.task_done()

# thread pool - declare and start
q = Queue()
for i in range(threads):
     t = Thread(target=worker)
     t.daemon = True
     t.start()

# generate alphanumeric permutations
print('Generating alphanumeric combos')
alpha = [''.join(p) for p in permutations('abcdefghijklmnopqrstuvwxyz',5)]
random.shuffle(alpha)
#user_ids = [''.join(p) for p in product(alpha, ['0','1','2','3','4','5','6','7','8','9'])]
print('Done enerating alphanumeric combos: ' + str(len(alpha)))

# create users
i = 0
for user_alpha in alpha:
  for user_i in range(0,9):
    user_name = user_alpha + str(user_i)
    user_uuid = uuid.uuid4().hex
    year = random.randint(2000, 2017)
    month = random.randint(1, 12)
    day = random.randint(1, 28)
    join_date = datetime(year, month, day)

    user = {}
    user['user_name'] = user_name
    user['join_date'] = join_date.strftime('%m/%d/%Y')
    user['user_uuid'] = user_uuid
    user['favorite_game'] = 'Fortnite'
    user['victory_royale'] = random.randint(0,100)
    user['losses'] = random.randint(0,1000)
    user_data = json.dumps(user)

    user_key = datastore_client.key(kind, user_name)
    user_entity = datastore.Entity(key=user_key,exclude_from_indexes=['data'])
    user_entity['data'] = user_data
    users.append(user_entity)
    if (i % 500 == 0):
      item = {}
      item['i'] = i
      item['users'] = users
      #print('Adding ' + str(i))
      q.put(item)
      #saver(i,datastore_client,users)
      users = []
    i = i + 1
      
#wait for all threads to finish
q.join() 
print ('Finished - saved ' + str(i) + ' records.')

