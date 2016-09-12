#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: op_with_action_and_workflow
  inputs:
    - city

  python_action:
    script: 'print "weather thing"'
  workflow: []

  outputs:
    - weather: "great!!"
    - temperature: "30"
  results:
    - SUCCESS: ${ 1 == 1 }
