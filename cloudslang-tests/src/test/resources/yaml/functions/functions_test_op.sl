#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: functions_test_op
  inputs:
    - exist
    - value_propagate_input:
        default: ${ value_propagate_input + get_sp('propagate.op.input') }
        private: true
  python_action:
    script: |
      language = 'CloudSlang'
  outputs:
    - output1_safe: ${ get('language', 'output1_default') }
    - output2_safe: ${ get('not_defined_key', 'output2_default') }
    - output_same_name: ${ get('output_same_name', 'output_same_name_default') }
    - output_1: ${get_sp('a.b.c.i_don_t_exist')}
    - output_2: ${get_sp('a.b.c.i_don_t_exist', 'default_str')}
    - output_3: ${get_sp('a.b.c.host')}
    - output_4: ${get_sp('a.b.c.host', 'default_str')}
    - output_5: ${get('i_don_exist', get_sp('a.b.c.host'))}
    - output_6: ${get('exist', get_sp('a.b.c.host'))}
    - output_7: ${get(    'i_don_exist',        get_sp('a.b.c.host')       )}
    - output_8: ${get_sp('a.b.c.i_don_exist', get_sp('a.b.c.host'))}
    - output_9: ${get_sp('a.b.c.null_value', 'default_str')}
    - value_propagate: ${ value_propagate_input + get_sp('propagate.op.output') }
    - output_10: ${get_sp('chars-b.c-hyphen')}
    - output_11: ${get_sp('chars-b.c-hyphen', 'default_str')}
    - output_12: ${get_sp("a.b.c.host")}
    - output_13: ${get_sp("a.b.c.host", 'default_str')}
    - output_14: ${get('i_dont_exist')}
    - output_15: ${check_empty(get('i_dont_exist'), 'default_str')}
  results:
    - FUNCTIONS_KEY_EXISTS: ${ get(get_sp('cloudslang.lang.key'), 'output1_default') == 'CloudSlang' }
    - FUNCTIONS_KEY_EXISTS_PROBLEM
