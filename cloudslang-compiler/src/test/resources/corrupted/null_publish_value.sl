#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

imports:
  ops: user.ops

flow:
  name: null_publish_value
  inputs:
    - input1
    - host
    - port: '22'
  workflow:
    - CheckWeather:
        do:
          ops.test_op:
            - city: 'input_1'
            - port
            - alla: 'walla'
        publish:
          - var_with_null_value:
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: FAILURE
  results:
    - SUCCESS
    - FAILURE
