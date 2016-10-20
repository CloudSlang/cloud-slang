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
  name: flow_with_on_failure
  inputs:
    - input1
  workflow:
    - first_step:
        do:
          ops.test_op:
            - city: 'input_1'
            - alla: 'walla'
        publish:
          - weather

    - second_step:
        do:
          ops.test_op:
            - city: 'input_1'
            - alla: 'walla'
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: FAILURE

    - on_failure:
        - check_something:
            do:
              ops.test_op:
                - city: 'a'
                - alla: 'a'
