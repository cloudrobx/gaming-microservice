#!/bin/bash -eu
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

# TODO: High-level file comment.

#!/bin/sh
curl -H "Content-Type: application/json" -X POST -d '{"user_name": "test", "losses": 999, "join_date": "04/11/2016", "favorite_game": "Fortnite", "victory_royale": 80, "user_uuid": "9143bc6e77f042fd8dd73b90155c68a4"}' https://user-go-dot-gaming-microservice.appspot.com/put?user=test

