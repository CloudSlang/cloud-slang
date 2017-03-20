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
  name: values_steps_modifiers
  inputs:
    # snake-case to camel-case
    - enable_option_for_action:
         required: false
    - enableOptionForAction:
        default: ${get("enable_option_for_action", "default_value")}
        private: true

    # helpers
    - output_no_expression_input: 'output_no_expression_value'
    - authorized_keys_path: './auth'
    - scp_host_port: '8888'
    - input_no_value_tag: 'input_no_value_tag_value'

    # properties
    - input_no_expression
    - input_no_expression_not_required:
        required: false
    - input_system_property: ${get_sp('user.sys.props.host')}
    - input_private:
        default: '25'
        private: true

    # loaded by Yaml
    - input_int: '22'
    - input_str_no_quotes: Hi
    - input_str_single: 'Hi'
    - input_str_double: "Hi"
    - input_yaml_list: '[1, 2, 3]'
    - input_properties_yaml_map_folded: {default: medium, required: false}
    - input_yaml_map:
        default: "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}"

    # evalauted via Python
    - input_python_null:
        default: ${ None }
        required: false
# uncomment when types will be supported
#    - input_python_list: ${[1, 2, 3]}
#    - input_python_map: >
#        ${{
#        'key1': 'value1',
#        'key2': 'value2',
#        'key3': 'value3'
#        }}
    - b: b
    - b_copy: ${ b }
    - input_concat_1: ${'a' + b}
    - input_concat_2_folded: >
        ${
        'prefix_' +
        input_concat_1 +
        '_suffix'
        }
    - input_expression_characters: >
        ${ 'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + authorized_keys_path + '} -p ' +
        scp_host_port + ':22 --name test1 -v /data:'}
    - step_argument_null: "step_argument_null_value"
  workflow:
    - step_standard:
        do:
          ops.values_op:
            # properties
            - input_no_expression

            # loaded by Yaml
            - input_int:
                value: '22'
            - input_str_no_quotes:
                value: Hi
                sensitive: false
            - input_str_single:
                value: 'Hi'
            - input_str_double:
                value: "Hi"
                sensitive: false
            - input_yaml_list:
                value: '[1, 2, 3]'
            - input_yaml_map_folded:
                value: "{key1: medium, key2: false}"
                sensitive: false

            # evaluated via Python
            - input_python_null:
                value: ${ None }
                sensitive: false
            - b:
                value: b
            - b_copy:
                value: ${ b }
            - input_concat_1:
                value: ${'a' + b}
                sensitive: false
            - input_concat_2_folded:
                value: >
                  ${
                  'prefix_' +
                  input_concat_1 +
                  '_suffix'
                  }
                sensitive: false
            - step_argument_null:
                value: null
            - input_no_value_tag:
                sensitive: false
        publish:
          - output_no_expression
          - publish_int: '22'
          - publish_str: publish_str_value
          - publish_expression: ${ publish_str + '_suffix' }
          - output_step_argument_null

  outputs:
    - output_no_expression
    - output_int: '22'
    - output_str: output_str_value
    - output_expression: ${ output_str + '_suffix' }
    - output_step_argument_null
  results:
    - SUCCESS
    - FAILURE
