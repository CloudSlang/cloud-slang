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
  name: sensitive_values_flow
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
    - input_b:
        default: b
        sensitive: true
    - input_b_copy: ${ input_b }
    - input_concat_1: ${'a' + input_b}
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
    - input_step_argument_null:
        default: "step_argument_null_value"
        sensitive: true
  workflow:
    - get_data:
        do:
          ops.noop:
            # properties
            - argument_no_expression
            - sensitive_argument_no_expression: ${input_no_expression}

            # loaded by Yaml
            - argument_int: 22
            - sensitive_argument_int: ${input_int}

            - argument_str_no_quotes: Hi
            - sensitive_argument_str_no_quotes: ${input_str_no_quotes}

            - argument_str_single: 'Hi'
            - sensitive_argument_str_single: ${input_str_single}

            - argument_str_double: "Hi"
            - sensitive_argument_str_double: ${input_str_double}

            - argument_yaml_list: [1, 2, 3]
            - sensitive_argument_yaml_list: ${input_yaml_list}

            - argument_properties_yaml_map_folded: {key1: medium, key2: false}
            - sensitive_argument_properties_yaml_map_folded: ${input_properties_yaml_map_folded}

            # evalauted via Python
            - argument_python_null: ${ None }
            - sensitive_argument_python_null: ${input_python_null}

            - argument_python_list: ${[1, 2, 3]}
            - sensitive_argument_python_list: ${input_python_list}

            - argument_python_map: >
                ${{
                'key1': 'value1',
                'key2': 'value2',
                'key3': 'value3'
                }}
            - sensitive_argument_python_map: ${input_python_map}

            - argument_b: b
            - sensitive_argument_b: ${input_b}

            - argument_b_copy: ${ argument_b }
            - sensitive_argument_b_copy: ${input_b_copy}

            - argument_concat_1: ${'a' + argument_b}
            - sensitive_argument_concat_1: ${input_concat_1}

            - argument_concat_2_folded: >
                ${
                'prefix_' +
                argument_concat_1 +
                '_suffix'
                }
            - sensitive_argument_concat_2_folded: ${input_concat_2_folded}

            - argument_step_argument_null: null
            - sensitive_argument_step_argument_null: ${input_step_argument_null}
        publish:
          - argument_no_expression
          - sensitive_argument_no_expression
          - argument_int
          - sensitive_argument_int
          - argument_str_no_quotes
          - sensitive_argument_str_no_quotes
          - argument_str_single
          - sensitive_argument_str_single
          - argument_str_double
          - sensitive_argument_str_double
          - argument_yaml_list
          - sensitive_argument_yaml_list
          - argument_properties_yaml_map_folded
          - sensitive_argument_properties_yaml_map_folded
          - argument_python_null
          - sensitive_argument_python_null
          - argument_python_list
          - sensitive_argument_python_list
          - argument_python_map
          - sensitive_argument_python_map
          - argument_b
          - sensitive_argument_b
          - argument_b_copy
          - sensitive_argument_b_copy
          - argument_concat_1
          - sensitive_argument_concat_1
          - argument_concat_2_folded
          - sensitive_argument_concat_2_folded
          - argument_step_argument_null
          - sensitive_argument_step_argument_null

    - print_raw_data:
        do:
          ops.print:
            - argument_no_expression: ${argument_no_expression}
            - sensitive_argument_no_expression: ${sensitive_argument_no_expression}
            - argument_int: ${argument_int}
            - sensitive_argument_int: ${sensitive_argument_int}
            - argument_str_no_quotes: ${argument_str_no_quotes}
            - sensitive_argument_str_no_quotes: ${sensitive_argument_str_no_quotes}
            - argument_str_single: ${argument_str_single}
            - sensitive_argument_str_single: ${sensitive_argument_str_single}
            - argument_str_double: ${argument_str_double}
            - sensitive_argument_str_double: ${sensitive_argument_str_double}
            - argument_yaml_list: ${argument_yaml_list}
            - sensitive_argument_yaml_list: ${sensitive_argument_yaml_list}
            - argument_properties_yaml_map_folded: ${argument_properties_yaml_map_folded}
            - sensitive_argument_properties_yaml_map_folded: ${sensitive_argument_properties_yaml_map_folded}
            - argument_python_null: ${argument_python_null}
            - sensitive_argument_python_null: ${sensitive_argument_python_null}
            - argument_python_list: ${argument_python_list}
            - sensitive_argument_python_list: ${sensitive_argument_python_list}
            - argument_python_map: ${argument_python_map}
            - sensitive_argument_python_map: ${sensitive_argument_python_map}
            - argument_b: ${argument_b}
            - sensitive_argument_b: ${sensitive_argument_b}
            - argument_b_copy: ${argument_b_copy}
            - sensitive_argument_b_copy: ${sensitive_argument_b_copy}
            - argument_concat_1: ${argument_concat_1}
            - sensitive_argument_concat_1: ${sensitive_argument_concat_1}
            - argument_concat_2_folded: ${argument_concat_2_folded}
            - sensitive_argument_concat_2_folded: ${sensitive_argument_concat_2_folded}
            - argument_step_argument_null: ${argument_step_argument_null}
            - sensitive_argument_step_argument_null: ${sensitive_argument_step_argument_null}

    - print_clear_text_data:
        do:
          ops.print:
            - argument_no_expression: ${argument_no_expression}
            - sensitive_argument_no_expression: ${sensitive_argument_no_expression}
            - argument_int: ${argument_int}
            - sensitive_argument_int: ${sensitive_argument_int}
            - argument_str_no_quotes: ${argument_str_no_quotes}
            - sensitive_argument_str_no_quotes: ${sensitive_argument_str_no_quotes}
            - argument_str_single: ${argument_str_single}
            - sensitive_argument_str_single: ${sensitive_argument_str_single}
            - argument_str_double: ${argument_str_double}
            - sensitive_argument_str_double: ${sensitive_argument_str_double}
            - argument_yaml_list: ${argument_yaml_list}
            - sensitive_argument_yaml_list: ${sensitive_argument_yaml_list}
            - argument_properties_yaml_map_folded: ${argument_properties_yaml_map_folded}
            - sensitive_argument_properties_yaml_map_folded: ${sensitive_argument_properties_yaml_map_folded}
            - argument_python_null: ${argument_python_null}
            - sensitive_argument_python_null: ${sensitive_argument_python_null}
            - argument_python_list: ${argument_python_list}
            - sensitive_argument_python_list: ${sensitive_argument_python_list}
            - argument_python_map: ${argument_python_map}
            - sensitive_argument_python_map: ${sensitive_argument_python_map}
            - argument_b: ${argument_b}
            - sensitive_argument_b: ${sensitive_argument_b}
            - argument_b_copy: ${argument_b_copy}
            - sensitive_argument_b_copy: ${sensitive_argument_b_copy}
            - argument_concat_1: ${argument_concat_1}
            - sensitive_argument_concat_1: ${sensitive_argument_concat_1}
            - argument_concat_2_folded: ${argument_concat_2_folded}
            - sensitive_argument_concat_2_folded: ${sensitive_argument_concat_2_folded}
            - argument_step_argument_null: ${argument_step_argument_null}
            - sensitive_argument_step_argument_null: ${sensitive_argument_step_argument_null}

  outputs:
    - argument_no_expression
    - sensitive_argument_no_expression
    - argument_int
    - sensitive_argument_int
    - argument_str_no_quotes
    - sensitive_argument_str_no_quotes
    - argument_str_single
    - sensitive_argument_str_single
    - argument_str_double
    - sensitive_argument_str_double
    - argument_yaml_list
    - sensitive_argument_yaml_list
    - argument_properties_yaml_map_folded
    - sensitive_argument_properties_yaml_map_folded
    - argument_python_null
    - sensitive_argument_python_null
    - argument_python_list
    - sensitive_argument_python_list
    - argument_python_map
    - sensitive_argument_python_map
    - argument_b
    - sensitive_argument_b
    - argument_b_copy
    - sensitive_argument_b_copy
    - argument_concat_1
    - sensitive_argument_concat_1
    - argument_concat_2_folded
    - sensitive_argument_concat_2_folded
    - argument_step_argument_null
    - sensitive_argument_step_argument_null
  results:
    - SUCCESS
    - FAILURE
