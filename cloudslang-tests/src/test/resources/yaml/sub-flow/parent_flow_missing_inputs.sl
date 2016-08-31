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
  name: parent_flow_missing_inputs
  inputs:
    - input1
    - city:
        required: false
  workflow:
    - step1:
        do:
          ops.check_weather:
            - missing_city: ${ city if city is not None else input1 }
        publish:
          - kuku: ${ weather }
    - step2:
        do:
          flows.child_flow:
            - input1: ${ kuku }
        publish:
          - val_output
    - step3:
        do:
          ops.check_number:
            - number: '4'
        navigate:
          - EVEN: SUCCESS
          - ODD: SUCCESS
          - FAILURE: FAILURE
