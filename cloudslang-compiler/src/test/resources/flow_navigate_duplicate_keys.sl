#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: slang.sample.flows

imports:
  ops: user.ops

flow:
  name: flow_navigate_duplicate_keys
  inputs:
  - city_name

  workflow:
    - CheckWeather1:
        do:
          ops.check_Weather:
            - city: city_name
            - country: 'Israel'
        publish:
          - weather
        navigate:
          - SUCCESS: RESULT1
          - FAILURE: RESULT2
          - SUCCESS: RESULT3
  outputs:
    - weather
  results:
    - RESULT1
    - RESULT2
    - RESULT3
