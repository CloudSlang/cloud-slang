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
  name: check_weather_flow
  inputs:
    - input_with_default_value:
            default: ""
            required: false
  workflow:
    - step1:
        do:
          ops.check_weather_required_input_with_default:
            - input_with_default_value
        publish:
          - kuku: ${ weather }
