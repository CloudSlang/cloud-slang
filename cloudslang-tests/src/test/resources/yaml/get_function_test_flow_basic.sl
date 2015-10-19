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
  name: get_function_test_flow_basic
  inputs:
    - input1:
        default: None
        overridable: false
        required: false
    - input1_safe:
        default: get('input1', 'input1_default')
        overridable: false
        required: false
    - input2:
        default: 22
        overridable: false
    - input2_safe:
        default: get('input2', 'input2_default')
        overridable: false
        required: false
    - input_locals_found:
        default: locals().get('input2', 'input_locals_found_default')
        overridable: false
        required: false
    - input_locals_not_found:
        default: locals().get('input2_i_dont_exist', 'input_locals_not_found_default')
        overridable: false
        required: false
  workflow:
    - Task1:
        do:
          ops.get_function_test_basic: []
        publish:
          - output1_safe
          - output2_safe
          - output_same_name
        navigate:
          GET_FUNCTION_KEY_EXISTS: GET_FUNCTION_KEY_EXISTS
          GET_FUNCTION_PROBLEM: GET_FUNCTION_PROBLEM
  results:
    - GET_FUNCTION_KEY_EXISTS
    - GET_FUNCTION_PROBLEM
