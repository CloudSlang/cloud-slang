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
  name: functions_test_flow
  inputs:
    - input1:
        default: "value1"
        private: true
        required: false
    - input1_safe:
        default: ${ get('input1_does_not_exist', 'input1_default') }
        private: true
        required: false
    - input2:
        default: '22'
        private: true
    - input2_safe:
        default: ${ get('input2', 'input2_default') }
        private: true
        required: false
    - input_locals_found:
        default: ${ locals().get('input2', 'input_locals_found_default') }
        private: true
        required: false
    - input_locals_not_found:
        default: ${ locals().get('input2_i_dont_exist', 'input_locals_not_found_default') }
        private: true
        required: false
    - exist
    - input_3:
        default: ${get_sp('a.b.c.i_don_t_exist')}
        required: false
    - input_4: ${get_sp('a.b.c.i_don_t_exist', 'default_str')}
    - input_5: ${get_sp('a.b.c.host')}
    - input_6: ${get_sp('a.b.c.host', 'default_str')}
    - input_7: ${get('i_don_exist', get_sp('a.b.c.host'))}
    - input_8: ${get('exist', get_sp('a.b.c.host'))}
    - input_9: ${get(    'i_don_exist',        get_sp('a.b.c.host')       )}
    - input_10: ${get_sp('a.b.c.i_don_exist', get_sp('a.b.c.host'))}
    - input_11: ${get_sp('a.b.c.null_value', 'default_str')}
    - value_propagate_input: ${ get_sp('propagate.flow.input') }
    - input_12: ${get_sp('chars-b.c-hyphen')}
    - input_13: ${get_sp('chars-b.c-hyphen', 'default_str')}
    - input_14: ${get_sp("a.b.c.host")}
    - input_15: ${get_sp("a.b.c.host", 'default_str')}
    - input_16:
        default: ${get('i_dont_exist')}
        private: true
        required: false
    - input_17: ${check_empty(get('i_dont_exist'), 'default_str')}
  workflow:
    - Step1:
        do:
          ops.functions_test_op:
            - exist
            - input_3: ${get_sp('a.b.c.i_don_t_exist')}
            - input_4: ${get_sp('a.b.c.i_don_t_exist', 'default_str')}
            - input_5: ${get_sp('a.b.c.host')}
            - input_6: ${get_sp('a.b.c.host', 'default_str')}
            - input_7: ${get('i_don_exist', get_sp('a.b.c.host'))}
            - input_8: ${get('exist', get_sp('a.b.c.host'))}
            - input_9: ${get(    'i_don_exist',        get_sp('a.b.c.host')       )}
            - input_10: ${get_sp('a.b.c.i_don_exist', get_sp('a.b.c.host'))}
            - input_11: ${get_sp('a.b.c.null_value', 'default_str')}
            - value_propagate_input: ${ value_propagate_input + get_sp('propagate.step.argument') }
            - input_12: ${get_sp('chars-b.c-hyphen')}
            - input_13: ${get_sp('chars-b.c-hyphen', 'default_str')}
            - input_14: ${get_sp("a.b.c.host")}
            - input_15: ${get_sp("a.b.c.host", 'default_str')}
            - input_16: ${get('i_dont_exist')}
            - input_17: ${check_empty(get('i_dont_exist'), 'default_str')}
        publish:
          - output1_safe
          - output2_safe
          - output_same_name
          - output_1
          - output_2
          - output_3
          - output_4
          - output_5
          - output_6
          - output_7
          - output_8
          - output_9
          - output_10
          - output_11
          - output_12
          - output_13
          - output_14
          - output_15
          - value_propagate: ${ value_propagate + get_sp('propagate.step.publish') }
        navigate:
          - FUNCTIONS_KEY_EXISTS: FUNCTIONS_KEY_EXISTS
          - FUNCTIONS_KEY_EXISTS_PROBLEM: FUNCTIONS_KEY_EXISTS_PROBLEM
  outputs:
    - value_propagate: ${ value_propagate + get_sp('propagate.flow.output') }
  results:
    - FUNCTIONS_KEY_EXISTS
    - FUNCTIONS_KEY_EXISTS_PROBLEM
