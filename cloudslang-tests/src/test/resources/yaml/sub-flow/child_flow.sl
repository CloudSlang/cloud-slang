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
  name: child_flow
  inputs:
    - input1: 'value'
    - time_zone_as_string: ${get_sp('user.sys.props.port')}
  workflow:
    - step01:
        do:
          ops.get_time_zone:
            - time_zone_as_string
        navigate:
          - SUCCESS: step02
          - NEGATIVE: FAILURE
    - step02:
        do:
          ops.test_op:
        navigate:
          - SUCCESS: SUCCESS
  outputs:
    - val_output: ${ input1 }
