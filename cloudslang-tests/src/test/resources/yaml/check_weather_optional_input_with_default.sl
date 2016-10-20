#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: check_weather_optional_input_with_default
  inputs:
    - city: "city"
    - input_with_default_value:
       default: "default_value"
       required: false
  python_action:
    script: |
      weather = "weather thing " + input_with_default_value
      print city
  outputs:
    - weather: ${ weather }
  results:
    - SUCCESS: ${ weather == "weather thing default_value" }
    - FAILURE
