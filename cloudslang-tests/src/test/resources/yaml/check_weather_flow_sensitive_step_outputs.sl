#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops
  flows: user.flows

flow:
  name: check_weather_flow_sensitive_step_outputs
  workflow:
    - bootstrap_node:
        do:
          ops.check_weather:
            - city: "CityName"
        navigate:
          - FAILURE: on_failure
          - SUCCESS: SUCCESS
        publish:
          - sensitive_step_output:
              value: '${weather}'
              sensitive: true
  outputs:
    - sensitive_flow_output:
        value: '${sensitive_step_output}'
  results:
    - FAILURE
    - SUCCESS