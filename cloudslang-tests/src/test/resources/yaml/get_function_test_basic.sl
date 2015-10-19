#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: get_function_test_basic
  action:
    python_script: |
        language = 'CloudSlang'
  outputs:
    - output1_safe: get('language', 'output1_default')
    - output2_safe: get('not_defined_key', 'output2_default')
    - output_same_name: get('output_same_name', 'output_same_name_default')
  results:
    - GET_FUNCTION_KEY_EXISTS: get('language', 'output1_default') == 'CloudSlang'
    - GET_FUNCTION_PROBLEM
