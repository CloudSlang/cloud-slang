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
  name: check_weather_flow_output_default_double
  inputs:
    - flow_input_1:
        default: "defaultValue"
    - flow_input_0:
        default: '${flow_input_1}'
  workflow:
    - bootstrap_node:
        do:
          ops.check_weather_required_input_sensitive:
            - input_with_sensitive_no_default
        navigate:
          - FAILURE: on_failure
          - SUCCESS: SUCCESS
        publish:
          - flow_output_1: '${weather}'
  outputs:
    - flow_output_0:
        value: 3.5
        sensitive: true
  results:
    - FAILURE
    - SUCCESS