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
  name: child_flow_missing_inputs
  inputs:
    - input1: 'value'
    - time_zone_as_string: ${get_sp('user.sys.props.port')}
  workflow:
    - step01:
        do:
          ops.get_time_zone:
            - missing_time_zone_as_string
    - step02:
        do:
          ops.test_op:
  outputs:
    - val_output: ${ input1 }
