#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops

flow:
  name: basic_flow
  inputs:
    - my_city:
        required: false
    - my_weather:
        required: false
  workflow:
    - CheckWeather:
        do:
          ops.check_weather:
            - city:
                required: false
                default: my_city
            - weather:
                required: false
                default: my_weather
