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
  name: flow_with_external_steps_and_errors
  workflow:
    - DoNothing:
        do_external:
        publish:
          - returnResult

        navigate:
          - SUCCESS: RealCheckWeather

    - RealCheckWeather:
        do:
          ops.java_op:
            - city: ${returnResult}
        do:
          ops.java_op:
            - city: ${returnResult}
        publish:
          - weather
        navigate:
          - SUCCESS: RealRealCheckWeather
          - FAILURE: FAILURE

    - RealRealCheckWeather:
        do:
          ops.check_Weather:
            - city: 'input_1'
        publish:
          - weather
          - temp: ${temperature}
          - publish_str: 'publish_str_value'
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: NotRealCheckWeather

    - NotRealCheckWeather:
        do:
          ops.check_Weather:
            - city: 'input_1'
        publish:
          - weather

  results:
    - SUCCESS
    - FAILURE