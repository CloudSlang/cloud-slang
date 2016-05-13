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
  name: get_function_test_flow_default_result
  workflow:
    - Step1:
        do:
          ops.get_function_test_default_result: []
        navigate:
          - GET_FUNCTION_DEFAULT_VALUE: GET_FUNCTION_DEFAULT_VALUE
          - GET_FUNCTION_PROBLEM: GET_FUNCTION_PROBLEM
  results:
    - GET_FUNCTION_DEFAULT_VALUE
    - GET_FUNCTION_PROBLEM
