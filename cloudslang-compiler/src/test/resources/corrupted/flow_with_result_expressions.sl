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
  name: flow_with_result_expressions
  inputs:
    - input1
    - host
    - port: '22'
  workflow:
    - CheckWeather1:
        do:
          ops.test_op:
            - city: 'input_1'
            - port
        publish:
          - weather
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: CheckWeather2
    - CheckWeather2:
        do:
          ops.test_op:
            - city: 'input_1'
            - port
        publish:
          - weather
        navigate:
          - SUCCESS: CUSTOM
          - FAILURE: FAILURE
  results:
    - SUCCESS: ${ 1 == 1 }
    - CUSTOM: ''
    - FAILURE
