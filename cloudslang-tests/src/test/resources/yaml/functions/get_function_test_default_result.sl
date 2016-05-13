#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: get_function_test_default_result
  python_action:
    script: |
      language = 'CloudSlang'
  results:
    - GET_FUNCTION_DEFAULT_VALUE: ${ get('key_not_found', 'key_not_found_default') == 'key_not_found_default' }
    - GET_FUNCTION_PROBLEM
