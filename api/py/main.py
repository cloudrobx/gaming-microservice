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
from flask import Flask, render_template, request, make_response
from flask_cors import CORS
import json
import logging
import timeit
# code you want to evaluate


from google.appengine.ext import ndb
from google.appengine.api import memcache

app = Flask(__name__)

@app.route('/get', methods=['GET'])
def handleGet():
    start_time = timeit.default_timer()
    user = request.args.get("user")

    user_data = getUser(user)
    r = make_response(user_data)
    r.headers['Content-Type'] = 'application/json'
    elapsed = timeit.default_timer() - start_time
    logging.info('GET Elapsed: ' + str(elapsed * 1000) + ' ms')
    return r

@app.route('/put', methods=['GET', 'POST'])
def handlePut():
    start_time = timeit.default_timer()
    user_name = request.args.get("user")
    content = request.get_json(silent=True)

    expected_keys = set(['user_name','victory_royale','losses','join_date','favorite_game','user_uuid'])
    if (not set(content.keys()) == expected_keys):
	return('Invalid JSON', 400)

    user = User(key=ndb.Key(User, user_name), data=json.dumps(content))
    user.put()
    logging.info('PUT Elapsed: ' + str(elapsed * 1000) + ' ms')
    return user_name

@app.route('/delete', methods=['GET', 'POST'])
def handleDelete():
    start_time = timeit.default_timer()
    user_name = request.args.get('user')
    user = loadUser(user_name)
    user.key.delete()
    logging.info('DELETE Elapsed: ' + str(elapsed * 1000) + ' ms')
    return user_name

def getUser(query_text):

    user = loadUser(query_text)

    if user:
       user_text = user.data
    else:
       user_text = '{"message":"user not found"}'
    
    return user_text
    
def loadUser(query_text):
    user_key = ndb.Key('User', query_text)
    start_time = timeit.default_timer()
    user = user_key.get()
    elapsed = timeit.default_timer() - start_time
    logging.info('LOAD Elapsed: ' + str(elapsed * 1000) + ' ms')

    return user

class User(ndb.Model):
    data = ndb.TextProperty()
    
    @classmethod
    def query_word(cls, ancestor_key):
        return cls.query(ancestor=ancestor_key)

