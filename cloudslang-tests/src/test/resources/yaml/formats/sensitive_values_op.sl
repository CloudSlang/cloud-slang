#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: sensitive_values_op
  inputs:
    # snake-case to camel-case
    - enable_option_for_action:
        required: false
        sensitive: true
    - enableOptionForAction:
        default: ${get("enable_option_for_action", "default_value")}
        overridable: false
        sensitive: true

    # helpers
    - output_no_expression:
        default: output_no_expression_value
        sensitive: true
    - authorized_keys_path:
        default: './auth'
        sensitive: true
    - scp_host_port:
        default: '8888'
        sensitive: true

    # properties
    - input_no_expression:
        sensitive: true
    - input_no_expression_not_required:
        required: false
        sensitive: true
    - input_system_property:
        default: ${get_sp('user.sys.props.host')}
        sensitive: true
    - input_not_overridable:
        default: 25
        overridable: false
        sensitive: true

    # loaded by Yaml
    - input_int:
        default: 22
        sensitive: true
    - input_str_no_quotes:
        default: Hi
        sensitive: true
    - input_str_single:
        default: 'Hi'
        sensitive: true
    - input_str_double:
        default: "Hi"
        sensitive: true
    - input_yaml_list:
        default: [1, 2, 3]
        sensitive: true
    - input_properties_yaml_map_folded:
        default: {default: medium, required: false}
        sensitive: true
    - input_yaml_map:
        default: {'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}
        sensitive: true

    # evalauted via Python
    - input_python_null:
        default: ${ None }
        required: false
        sensitive: true
    - input_python_list:
        default: ${[1, 2, 3]}
        sensitive: true
    - input_python_map: >
        ${{
        'key1': 'value1',
        'key2': 'value2',
        'key3': 'value3'
        }}
        sensitive: true
    - b:
        default: b
        sensitive: true
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
        sensitive: true
    - step_argument_null:
        default: "step_argument_null_value"
        sensitive: true
  action:
    python_script: result = 'success'
  outputs:
    - output_no_expression
    - output_int: 22
    - output_str: output_str_value
    - output_expression: ${ output_str + '_suffix' }
    - output_step_argument_null: ${step_argument_null}
  results:
    - SUCCESS: ${ result == 'success' }
    - FAILURE
