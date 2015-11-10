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
  name: values_flow
  inputs:
    # properties
    - input_no_expression
    - input_no_expression_not_required:
        required: false
    - input_system_property:
        system_property: user.sys.props.host
    - input_not_overridable:
        default: 25
        overridable: false

    # loaded by Yaml
    - input_int: 22
    - input_str_no_quotes: Hi
    - input_str_single: 'Hi'
    - input_str_double: "Hi"
    - input_yaml_list: [1, 2, 3]
    - input_properties_yaml_map_folded: {default: medium, required: false}

    # evalauted via Python
    - input_python_null:
        default: ${ None }
        required: false
    - input_python_list: ${[1, 2, 3]}
    - input_python_map: >
        ${{
        'key1': 'value1',
        'key2': 'value2',
        'key3': 'value3'
        }}
    - input_python_map_quotes: "${{'value': 2}}"
    - b: b
    - b_copy: ${ b }
    - input_concat_1: ${'a' + b}
    - input_concat_2_one_liner: ${'prefix_' + input_concat_1 + '_suffix'}
    - input_concat_2_folded: >
        ${
        'prefix_' +
        input_concat_1 +
        '_suffix'
        }

    # helpers
    - output_no_expression: output_no_expression_value
  workflow:
    - task_standard:
        do:
          ops.values_op:
            # properties
            - input_no_expression

            # loaded by Yaml
            - input_int: 22
            - input_str_no_quotes: Hi
            - input_str_single: 'Hi'
            - input_str_double: "Hi"
            - input_yaml_list: [1, 2, 3]
            - input_yaml_map_folded: {key1: medium, key2: false}

            # evalauted via Python
            - input_python_null: ${ None }
            - input_python_list: ${[1, 2, 3]}
            - input_python_map: >
                ${{
                'key1': 'value1',
                'key2': 'value2',
                'key3': 'value3'
                }}
            - input_python_map_quotes: "${{'value': 2}}"
            - b: b
            - b_copy: ${ b }
            - input_concat_1: ${'a' + b}
            - input_concat_2_one_liner: ${'prefix_' + input_concat_1 + '_suffix'}
            - input_concat_2_folded: >
                ${
                'prefix_' +
                input_concat_1 +
                '_suffix'
                }
        publish:
          - output_no_expression
          - publish_int: 22
          - publish_str: publish_str_value
          - publish_expression: ${ publish_str + '_suffix' }

    - task_one_liner:
        do:
          ops.noop: input_no_expression, input_int = ${ 22 }, input_expression = ${ input_no_expression + '_suffix' }
  outputs:
    - output_no_expression
    - output_int: 22
    - output_str: output_str_value
    - output_expression: ${ output_str + '_suffix' }
  results:
    - SUCCESS
    - FAILURE
