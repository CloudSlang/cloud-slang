#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

imports:
  ops: user.ops

flow:
  name: flow_both_result_and_step_name
  workflow:
    - print_message1:
        do:
          ops.test_op:
            - alla: 'message 1'
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: BOTH_RESULT_AND_STEP_NAME

    - BOTH_RESULT_AND_STEP_NAME:
        do:
          ops.test_op:
            - alla: 'message 1'
        navigate:
          - SUCCESS: print_message2
          - FAILURE: print_message2

    - print_message2:
        do:
          ops.test_op:
            - alla: 'message 1'
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: SUCCESS
  results:
    - SUCCESS
    - BOTH_RESULT_AND_STEP_NAME
