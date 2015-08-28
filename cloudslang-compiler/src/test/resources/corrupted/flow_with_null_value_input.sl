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
  name: flow_with_null_value_input
  inputs:
    - input1:
  workflow:
    - CheckWeather:
        do:
          ops.check_Weather:
            - city: city_name
            - country: str('Israel')
        publish:
          - weather
  outputs:
    - weather
