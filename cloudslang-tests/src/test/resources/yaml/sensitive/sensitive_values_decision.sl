#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

decision:
  name: sensitive_values_decision

  inputs:
    - input_no_value:
         required: false
         sensitive: false
    - input_no_value_sensitive:
         required: false
         sensitive: true

    - input_get_input_no_value:
        default: ${input_no_value}
        required: false
        private: true
    - input_get_input_no_value_sensitive:
        default: ${input_no_value}
        required: false
        private: true
        sensitive: true
    - input_get_sensitive_input_no_value:
        default: ${input_no_value_sensitive}
        required: false
    - input_get_sensitive_input_no_value_sensitive:
        default: ${input_no_value_sensitive}
        required: false
        sensitive: true

    - input_python_get_input_no_value:
        default: ${get("input_no_value", "default_value")}
        private: true
    - input_python_get_input_no_value_sensitive:
        default: ${get("input_no_value", "default_value")}
        private: true
        sensitive: true
    - input_python_get_sensitive_input_no_value:
        default: ${get("input_no_value_sensitive", "default_value")}
    - input_python_get_sensitive_input_no_value_sensitive:
        default: ${get("input_no_value_sensitive", "default_value")}
        sensitive: true

    - input_with_value:
        default: 'aaa'
    - input_with_value_sensitive:
        default: 'aaa'
        sensitive: true

    - input_get_input_with_value:
        default: ${input_with_value}
        private: true
    - input_get_input_with_value_sensitive:
        default: ${input_with_value}
        private: true
        sensitive: true
    - input_get_sensitive_input_with_value:
        default: ${input_with_value_sensitive}
    - input_get_sensitive_input_with_value_sensitive:
        default: ${input_with_value_sensitive}
        sensitive: true

    - input_python_get_input_with_value:
        default: ${get("input_with_value", "default_value")}
        private: true
    - input_python_get_input_with_value_sensitive:
        default: ${get("input_with_value", "default_value")}
        private: true
        sensitive: true
    - input_python_get_sensitive_input_with_value:
        default: ${get("input_with_value_sensitive", "default_value")}
    - input_python_get_sensitive_input_with_value_sensitive:
        default: ${get("input_with_value_sensitive", "default_value")}
        sensitive: true

    - input_system_property:
        default: ${get_sp('user.sys.props.host')}
    - input_system_property_sensitive:
        default: ${get_sp('user.sys.props.host')}
        sensitive: true

    - input_get_input_system_property:
        default: ${input_system_property}
    - input_get_input_system_property_sensitive:
        default: ${input_system_property}
        sensitive: true
    - input_get_sensitive_input_system_property:
        default: ${input_system_property_sensitive}
    - input_get_sensitive_input_system_property_sensitive:
        default: ${input_system_property_sensitive}
        sensitive: true

    - input_python_get_input_system_property:
        default: ${get("input_system_property", "default_value")}
    - input_python_get_input_system_property_sensitive:
        default: ${get("input_system_property", "default_value")}
        sensitive: true
    - input_python_get_sensitive_input_system_property:
        default: ${get("input_system_property_sensitive", "default_value")}
    - input_python_get_sensitive_input_system_property_sensitive:
        default: ${get("input_system_property_sensitive", "default_value")}
        sensitive: true

    - input_authorized_keys_path:
        default: './auth'
        sensitive: false
    - input_authorized_keys_path_sensitive:
        default: './auth'
        sensitive: true

    - input_scp_host_port:
        default: '8888'
    - input_scp_host_port_sensitive:
        default: '8888'
        sensitive: true

    - input_not_overridable:
        default: '10'
        private: true
        sensitive: false
    - input_not_overridable_sensitive:
        default: '10'
        private: true
        sensitive: true

    - input_overridable_hi1:
        default: 'Hi'
        private: false
        sensitive: false
    - input_overridable_hi1_sensitive:
        default: 'Hi'
        private: false
        sensitive: true

    - input_overridable_hi2:
        default: 'Hi'
        private: false
        sensitive: false
    - input_overridable_hi2_sensitive:
        default: 'Hi'
        private: false
        sensitive: true

    - input_overridable_hi3:
        default: "Hi"
        private: false
        sensitive: false
    - input_overridable_hi3_sensitive:
        default: "Hi"
        private: false
        sensitive: true

    - input_yaml_list:
        default: '[1, 2, 3]'
    - input_yaml_list_sensitive:
        default: '[1, 2, 3]'
        sensitive: true

    - input_yaml_map:
        default: "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}"
    - input_yaml_map_sensitive:
        default: "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}"
        sensitive: true

    - input_properties_yaml_map_folded:
        default: "{default: medium, required: false}"
    - input_properties_yaml_map_folded_sensitive:
        default: "{default: medium, required: false}"
        sensitive: true

    - input_python_null:
        default: ${ None }
        required: false
    - input_python_null_sensitive:
        default: ${ None }
        required: false
        sensitive: true
# uncomment when types will be supported
#    - input_python_list:
#        default: ${[1, 2, 3]}
#    - input_python_list_sensitive:
#        default: ${[1, 2, 3]}
#        sensitive: true
#
#    - input_python_map:
#        default: >
#          ${{
#          'key1': 'value1',
#          'key2': 'value2',
#          'key3': 'value3'
#          }}
#    - input_python_map_sensitive:
#        default: >
#          ${{
#          'key1': 'value1',
#          'key2': 'value2',
#          'key3': 'value3'
#          }}
#        sensitive: true

    - input_x: x
    - input_x_sensitive:
        default: x
        sensitive: true

    - input_x_copy: ${ input_x }
    - input_x_copy_sensitive:
        default: ${ input_x }
        sensitive: true
    - input_sensitive_x_copy:
        default: ${ input_x_sensitive }
    - input_sensitive_x_copy_sensitive:
        default: ${ input_x_sensitive }
        sensitive: true

    - input_concat_x: ${'a' + input_x}
    - input_concat_x_sensitive:
        default: ${'a' + input_x}
        sensitive: true
    - input_sensitive_concat_x: ${'a' + input_x_sensitive}
    - input_sensitive_concat_x_sensitive:
        default: ${'a' + input_x_sensitive}
        sensitive: true

    - input_y: 'y'
    - input_y_sensitive:
        default: 'y'
        sensitive: true

    - input_concat_y: ${input_y + "b"}
    - input_concat_y_sensitive:
        default: ${input_y + "b"}
        sensitive: true
    - input_sensitive_concat_y: ${input_y_sensitive + "b"}
    - input_sensitive_concat_y_sensitive:
        default: ${input_y_sensitive + "b"}
        sensitive: true

    - input_concat_ab: ${'a' + 'b'}
    - input_concat_ab_sensitive:
        default: ${'a' + 'b'}
        sensitive: true

    - input_concat_xy: ${input_x + input_y}
    - input_concat_xy_sensitive:
        default: ${input_x + input_y}
        sensitive: true
    - input_concat_x_sensitive_y: ${input_x_sensitive + input_y}
    - input_concat_x_y_sensitive: ${input_x + input_y_sensitive}
    - input_concat_x_y_sensitive_sensitive:
        default: ${input_x + input_y_sensitive}
        sensitive: true
    - input_concat_x_sensitive_y_sensitive_sensitive:
        default: ${input_x_sensitive + input_y_sensitive}
        sensitive: true

    - input_concat_x_folded: >
        ${
        'prefix_' +
        input_concat_x +
        '_suffix'
        }
    - input_concat_x_folded_sensitive:
        default: >
          ${
          'prefix_' +
          input_concat_x +
          '_suffix'
          }
        sensitive: true
    - input_sensitive_concat_x_folded: >
        ${
        'prefix_' +
        input_concat_x_sensitive +
        '_suffix'
        }
    - input_concat_x_folded_copy:
        default: ${ input_concat_x_folded }
    - input_concat_x_folded_copy_sensitive:
        default: ${ input_concat_x_folded }
        sensitive: true
    - input_sensitive_concat_x_folded_copy:
        default: ${ input_concat_x_folded_sensitive }
    - input_sensitive_concat_x_folded_sensitive:
        default: ${ input_concat_x_folded_sensitive }
        sensitive: true

    - input_expression_characters:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + ':22 --name test1 -v /data:'}
    - input_expression_characters_sensitive:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + ':22 --name test1 -v /data:'}
        sensitive: true
    - input_sensitive_expression_characters:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path_sensitive + '} -p ' + input_scp_host_port_sensitive + ':22 --name test1 -v /data:'}
    - input_sensitive_expression_characters_sensitive:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path_sensitive + '} -p ' + input_scp_host_port_sensitive + ':22 --name test1 -v /data:'}
        sensitive: true

  outputs:
    - output_input_no_value: ${input_no_value}
    - output_sensitive_input_no_value:
        value: ${input_no_value}
        sensitive: true
    - output_input_no_value_sensitive: ${input_no_value_sensitive}
    - output_sensitive_input_no_value_sensitive:
        value: ${input_no_value_sensitive}
        sensitive: true

    - output_input_get_input_no_value:
        value: ${input_get_input_no_value}
    - output_sensitive_input_get_input_no_value:
        value: ${input_get_input_no_value}
        sensitive: true
    - output_input_get_input_no_value_sensitive:
        value: ${input_get_input_no_value_sensitive}
        sensitive: false
    - output_sensitive_input_get_input_no_value_sensitive:
        value: ${input_get_input_no_value_sensitive}
        sensitive: true
    - output_input_get_sensitive_input_no_value:
        value: ${input_get_sensitive_input_no_value}
    - output_sensitive_input_get_sensitive_input_no_value:
        value: ${input_get_sensitive_input_no_value}
        sensitive: true
    - output_input_get_sensitive_input_no_value_sensitive:
        value: ${input_get_sensitive_input_no_value_sensitive}
        sensitive: false
    - output_sensitive_input_get_sensitive_input_no_value_sensitive:
        value: ${input_get_sensitive_input_no_value_sensitive}
        sensitive: true

    - output_python_get_input_no_value: ${get("input_no_value", "default_value")}
    - output_sensitive_python_get_input_no_value:
        value: ${get("input_no_value", "default_value")}
        sensitive: true
    - output_python_get_sensitive_input_no_value: ${get("input_no_value_sensitive", "default_value")}
    - output_sensitive_python_get_sensitive_input_no_value:
        value: ${get("input_no_value_sensitive", "default_value")}
        sensitive: true
    - output_input_python_get_input_no_value: ${input_python_get_input_no_value}
    - output_sensitive_input_python_get_input_no_value:
        value: ${input_python_get_input_no_value}
        sensitive: true
    - output_input_python_get_input_no_value_sensitive: ${input_python_get_input_no_value_sensitive}
    - output_sensitive_input_python_get_input_no_value_sensitive:
        value: ${input_python_get_input_no_value_sensitive}
        sensitive: true
    - output_input_python_get_sensitive_input_no_value: ${input_python_get_sensitive_input_no_value}
    - output_sensitive_input_python_get_sensitive_input_no_value:
        value: ${input_python_get_sensitive_input_no_value}
        sensitive: true
    - output_input_python_get_sensitive_input_no_value_sensitive: ${input_python_get_sensitive_input_no_value_sensitive}
    - output_sensitive_input_python_get_sensitive_input_no_value_sensitive:
        value: ${input_python_get_sensitive_input_no_value_sensitive}
        sensitive: true

    - output_with_value: 'bbb'
    - output_sensitive_with_value:
        value: 'bbb'
        sensitive: true
    - output_input_with_value: ${input_with_value}
    - output_sensitive_input_with_value:
        value: ${input_with_value}
        sensitive: true
    - output_input_with_value_sensitive: ${input_with_value_sensitive}
    - output_sensitive_input_with_value_sensitive:
        value: ${input_with_value_sensitive}
        sensitive: true

    - output_input_get_input_with_value: ${input_get_input_with_value}
    - output_sensitive_input_get_input_with_value:
        value: ${input_get_input_with_value}
        sensitive: true
    - output_input_get_input_with_value_sensitive: ${input_get_input_with_value_sensitive}
    - output_sensitive_input_get_input_with_value_sensitive:
        value: ${input_get_input_with_value_sensitive}
        sensitive: true
    - output_input_get_sensitive_input_with_value: ${input_get_sensitive_input_with_value}
    - output_sensitive_input_get_sensitive_input_with_value:
        value: ${input_get_sensitive_input_with_value}
        sensitive: true
    - output_input_get_sensitive_input_with_value_sensitive: ${input_get_sensitive_input_with_value_sensitive}
    - output_sensitive_input_get_sensitive_input_with_value_sensitive:
        value: ${input_get_sensitive_input_with_value_sensitive}
        sensitive: true

    - output_python_get_input_with_value: ${get("input_with_value", "default_value")}
    - output_sensitive_python_get_input_with_value:
        value: ${get("input_with_value", "default_value")}
        sensitive: true
    - output_python_get_input_with_value_sensitive: ${get("input_with_value_sensitive", "default_value")}
    - output_sensitive_python_get_input_with_value_sensitive:
        value: ${get("input_with_value_sensitive", "default_value")}
        sensitive: true
    - output_input_python_get_input_with_value: ${input_python_get_input_with_value}
    - output_sensitive_input_python_get_input_with_value:
        value: ${input_python_get_input_with_value}
        sensitive: true
    - output_input_python_get_input_with_value_sensitive: ${input_python_get_input_with_value_sensitive}
    - output_sensitive_input_python_get_input_with_value_sensitive:
        value: ${input_python_get_input_with_value_sensitive}
        sensitive: true
    - output_input_python_get_sensitive_input_with_value: ${input_python_get_sensitive_input_with_value}
    - output_sensitive_input_python_get_sensitive_input_with_value:
        value: ${input_python_get_sensitive_input_with_value}
        sensitive: true
    - output_input_python_get_sensitive_input_with_value_sensitive: ${input_python_get_sensitive_input_with_value_sensitive}
    - output_sensitive_input_python_get_sensitive_input_with_value_sensitive:
        value: ${input_python_get_sensitive_input_with_value_sensitive}
        sensitive: true

    - output_system_property: ${get_sp('user.sys.props.host')}
    - output_sensitive_system_property:
        value: ${get_sp('user.sys.props.host')}
        sensitive: true
    - output_input_system_property: ${input_system_property}
    - output_sensitive_input_system_property:
        value: ${input_system_property}
        sensitive: true
    - output_input_system_property_sensitive: ${input_system_property_sensitive}
    - output_sensitive_input_system_property_sensitive:
        value: ${input_system_property_sensitive}
        sensitive: true

    - output_get_input_system_property: ${input_system_property}
    - output_sensitive_get_input_system_property:
        value: ${input_system_property}
        sensitive: true
    - output_get_input_system_property_sensitive: ${input_system_property_sensitive}
    - output_sensitive_get_input_system_property_sensitive:
        value: ${input_system_property_sensitive}
        sensitive: true
    - output_input_get_input_system_property: ${input_get_input_system_property}
    - output_sensitive_input_get_input_system_property:
        value: ${input_get_input_system_property}
        sensitive: true
    - output_input_get_input_system_property_sensitive: ${input_get_input_system_property_sensitive}
    - output_sensitive_input_get_input_system_property_sensitive:
        value: ${input_get_input_system_property_sensitive}
        sensitive: true
    - output_input_get_sensitive_input_system_property: ${input_get_sensitive_input_system_property}
    - output_sensitive_input_get_sensitive_input_system_property:
        value: ${input_get_sensitive_input_system_property}
        sensitive: true
    - output_input_get_sensitive_input_system_property_sensitive: ${input_get_sensitive_input_system_property_sensitive}
    - output_sensitive_input_get_sensitive_input_system_property_sensitive:
        value: ${input_get_sensitive_input_system_property_sensitive}
        sensitive: true

    - output_python_get_input_system_property: ${get("input_system_property", "default_value")}
    - output_sensitive_python_get_input_system_property:
        value: ${get("input_system_property", "default_value")}
        sensitive: true
    - output_python_get_input_system_property_sensitive: ${get("input_system_property_sensitive", "default_value")}
    - output_sensitive_python_get_input_system_property_sensitive:
        value: ${get("input_system_property_sensitive", "default_value")}
        sensitive: true
    - output_input_python_get_input_system_property: ${input_python_get_input_system_property}
    - output_sensitive_input_python_get_input_system_property:
        value: ${input_python_get_input_system_property}
        sensitive: true
    - output_input_python_get_input_system_property_sensitive: ${input_python_get_input_system_property_sensitive}
    - output_sensitive_input_python_get_input_system_property_sensitive:
        value: ${input_python_get_input_system_property_sensitive}
        sensitive: true
    - output_input_python_get_sensitive_input_system_property: ${input_python_get_sensitive_input_system_property}
    - output_sensitive_input_python_get_sensitive_input_system_property:
        value: ${input_python_get_sensitive_input_system_property}
        sensitive: true
    - output_input_python_get_sensitive_input_system_property_sensitive: ${input_python_get_sensitive_input_system_property_sensitive}
    - output_sensitive_input_python_get_sensitive_input_system_property_sensitive:
        value: ${input_python_get_sensitive_input_system_property_sensitive}
        sensitive: true

    - output_authorized_keys_path: './auth'
    - output_sensitive_authorized_keys_path:
        value: './auth'
        sensitive: true
    - output_input_authorized_keys_path: ${input_authorized_keys_path}
    - output_sensitive_input_authorized_keys_path:
        value: ${input_authorized_keys_path}
        sensitive: true
    - output_input_authorized_keys_path_sensitive: ${input_authorized_keys_path_sensitive}
    - output_sensitive_input_authorized_keys_path_sensitive:
        value: ${input_authorized_keys_path_sensitive}
        sensitive: true

    - output_scp_host_port: '9999'
    - output_sensitive_scp_host_port:
        value: '9999'
        sensitive: true
    - output_input_scp_host_port: ${input_scp_host_port}
    - output_sensitive_input_scp_host_port:
        value: ${input_scp_host_port}
        sensitive: true
    - output_input_scp_host_port_sensitive: ${input_scp_host_port_sensitive}
    - output_sensitive_input_scp_host_port_sensitive:
        value: ${input_scp_host_port_sensitive}
        sensitive: true

    - output_input_not_overridable: ${input_not_overridable}
    - output_sensitive_input_not_overridable:
        value: ${input_not_overridable}
        sensitive: true
    - output_input_not_overridable_sensitive: ${input_not_overridable_sensitive}
    - output_sensitive_input_not_overridable_sensitive:
        value: ${input_not_overridable_sensitive}
        sensitive: true

    - output_overridable_hi1: Hi
    - output_sensitive_overridable_hi1:
        value: Hi
        sensitive: true
    - output_input_overridable_hi1: ${input_overridable_hi1}
    - output_sensitive_input_overridable_hi1:
        value: ${input_overridable_hi1}
        sensitive: true
    - output_input_overridable_hi1_sensitive: ${input_overridable_hi1_sensitive}
    - output_sensitive_input_overridable_hi1_sensitive:
        value: ${input_overridable_hi1_sensitive}
        sensitive: true

    - output_overridable_hi2: 'Hi'
    - output_sensitive_overridable_hi2:
        value: 'Hi'
        sensitive: true
    - output_input_overridable_hi2: ${input_overridable_hi2}
    - output_sensitive_input_overridable_hi2:
        value: ${input_overridable_hi2}
        sensitive: true
    - output_input_overridable_hi2_sensitive: ${input_overridable_hi2_sensitive}
    - output_sensitive_input_overridable_hi2_sensitive:
        value: ${input_overridable_hi2_sensitive}
        sensitive: true

    - output_overridable_hi3: "Hi"
    - output_sensitive_overridable_hi3:
        value: "Hi"
        sensitive: true
    - output_input_overridable_hi3: ${input_overridable_hi3}
    - output_sensitive_input_overridable_hi3:
        value: ${input_overridable_hi3}
        sensitive: true
    - output_input_overridable_hi3_sensitive: ${input_overridable_hi3_sensitive}
    - output_sensitive_input_overridable_hi3_sensitive:
        value: ${input_overridable_hi3_sensitive}
        sensitive: true

    - output_yaml_list: '[1, 2, 3]'
    - output_sensitive_yaml_list:
        value: '[1, 2, 3]'
        sensitive: true
    - output_input_yaml_list: ${input_yaml_list}
    - output_sensitive_input_yaml_list:
        value: ${input_yaml_list}
        sensitive: true
    - output_input_yaml_list_sensitive: ${input_yaml_list_sensitive}
    - output_sensitive_input_yaml_list_sensitive:
        value: ${input_yaml_list_sensitive}
        sensitive: true

    - output_yaml_map:
        value: "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}"
    - output_sensitive_yaml_map:
        value: "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}"
        sensitive: true
    - output_input_yaml_map: ${input_yaml_map}
    - output_sensitive_input_yaml_map:
        value: ${input_yaml_map}
        sensitive: true
    - output_input_yaml_map_sensitive: ${input_yaml_map_sensitive}
    - output_sensitive_input_yaml_map_sensitive:
        value: ${input_yaml_map_sensitive}
        sensitive: true

    - output_properties_yaml_map_folded:
        value: "{default: medium, required: false}"
    - output_sensitive_properties_yaml_map_folded:
        value: "{default: medium, required: false}"
        sensitive: true
    - output_input_properties_yaml_map_folded: ${input_properties_yaml_map_folded}
    - output_sensitive_input_properties_yaml_map_folded:
        value: ${input_properties_yaml_map_folded}
        sensitive: true
    - output_input_properties_yaml_map_folded_sensitive: ${input_properties_yaml_map_folded_sensitive}
    - output_sensitive_input_properties_yaml_map_folded_sensitive:
        value: ${input_properties_yaml_map_folded_sensitive}
        sensitive: true

    - output_python_null: ${ None }
    - output_sensitive_python_null:
        value: ${ None }
        sensitive: true
    - output_input_python_null: ${input_python_null}
    - output_sensitive_input_python_null:
        value: ${input_python_null}
        sensitive: true
    - output_input_python_null_sensitive: ${input_python_null_sensitive}
    - output_sensitive_input_python_null_sensitive:
        value: ${input_python_null_sensitive}
        sensitive: true

#    - output_python_list: ${[1, 2, 3]}
#    - output_sensitive_python_list:
#        value: ${[1, 2, 3]}
#        sensitive: true
#    - output_input_python_list: ${input_python_list}
#    - output_sensitive_input_python_list:
#        value: ${input_python_list}
#        sensitive: true
#    - output_input_python_list_sensitive: ${input_python_list_sensitive}
#    - output_sensitive_input_python_list_sensitive:
#        value: ${input_python_list_sensitive}
#        sensitive: true

#    - output_python_map: ${{'key1':'value1', 'key2':'value2', 'key3':'value3'}}
#    - output_sensitive_python_map:
#        value: ${{'key1':'value1', 'key2':'value2', 'key3':'value3'}}
#        sensitive: true
#    - output_input_python_map: ${input_python_map}
#    - output_sensitive_input_python_map:
#        value: ${input_python_map}
#        sensitive: true
#    - output_input_python_map_sensitive: ${input_python_map_sensitive}
#    - output_sensitive_input_python_map_sensitive:
#        value: ${input_python_map_sensitive}
#        sensitive: true

    - output_x: x
    - output_sensitive_x:
        value: x
        sensitive: true
    - output_input_x: ${input_x}
    - output_sensitive_input_x:
        value: ${input_x}
        sensitive: true
    - output_input_x_sensitive: ${input_x_sensitive}
    - output_sensitive_input_x_sensitive:
        value: ${input_x_sensitive}
        sensitive: true
    - output_input_x_copy: ${input_x_copy}
    - output_sensitive_input_x_copy:
        value: ${input_x_copy}
        sensitive: true
    - output_input_x_copy_sensitive: ${input_x_copy_sensitive}
    - output_sensitive_input_x_copy_sensitive:
        value: ${input_x_copy_sensitive}
        sensitive: true
    - output_input_sensitive_x_copy: ${input_sensitive_x_copy}
    - output_sensitive_input_sensitive_x_copy:
        value: ${input_sensitive_x_copy}
        sensitive: true
    - output_input_sensitive_x_copy_sensitive: ${input_sensitive_x_copy_sensitive}
    - output_sensitive_input_sensitive_x_copy_sensitive:
        value: ${input_sensitive_x_copy_sensitive}
        sensitive: true

    - output_concat_x: ${'a' + input_x}
    - output_sensitive_concat_x:
        value: ${'a' + input_x}
        sensitive: true
    - output_input_concat_x: ${input_concat_x}
    - output_sensitive_input_concat_x:
        value: ${input_concat_x}
        sensitive: true
    - output_input_concat_x_sensitive: ${input_concat_x_sensitive}
    - output_sensitive_input_concat_x_sensitive:
        value: ${input_concat_x_sensitive}
        sensitive: true
    - output_input_sensitive_concat_x: ${input_sensitive_concat_x}
    - output_sensitive_input_sensitive_concat_x:
        value: ${input_sensitive_concat_x}
        sensitive: true
    - output_input_sensitive_concat_x_sensitive: ${input_sensitive_concat_x_sensitive}
    - output_sensitive_input_sensitive_concat_x_sensitive:
        value: ${input_sensitive_concat_x_sensitive}
        sensitive: true

    - output_y: y
    - output_sensitive_y:
        value: y
        sensitive: true
    - output_concat_y: ${input_y + "b"}
    - output_sensitive_concat_y:
        value: ${input_y + "b"}
        sensitive: true
    - output_input_concat_y: ${input_concat_y}
    - output_sensitive_input_concat_y:
        value: ${input_concat_y}
        sensitive: true
    - output_input_concat_y_sensitive: ${input_concat_y_sensitive}
    - output_sensitive_input_concat_y_sensitive:
        value: ${input_concat_y_sensitive}
        sensitive: true
    - output_input_sensitive_concat_y: ${input_sensitive_concat_y}
    - output_sensitive_input_sensitive_concat_y:
        value: ${input_sensitive_concat_y}
        sensitive: true
    - output_input_sensitive_concat_y_sensitive: ${input_sensitive_concat_y_sensitive}
    - output_sensitive_input_sensitive_concat_y_sensitive:
        value: ${input_sensitive_concat_y_sensitive}
        sensitive: true

    - output_concat_ab: ${'a' + 'b'}
    - output_sensitive_concat_ab:
        value: ${'a' + 'b'}
        sensitive: true
    - output_input_concat_ab: ${input_concat_ab}
    - output_sensitive_input_concat_ab:
        value: ${input_concat_ab}
        sensitive: true
    - output_input_concat_ab_sensitive: ${input_concat_ab_sensitive}
    - output_sensitive_input_concat_ab_sensitive:
        value: ${input_concat_ab_sensitive}
        sensitive: true

    - output_concat_xy: ${input_x + input_y}
    - output_sensitive_concat_xy:
        value: ${input_x + input_y}
        sensitive: true
    - output_input_concat_xy: ${input_concat_xy}
    - output_sensitive_input_concat_xy:
        value: ${input_concat_xy}
        sensitive: true
    - output_input_concat_xy_sensitive: ${input_concat_xy_sensitive}
    - output_sensitive_input_concat_xy_sensitive:
        value: ${input_concat_xy_sensitive}
        sensitive: true
    - output_concat_x_sensitive_y: ${input_x_sensitive + input_y}
    - output_sensitive_concat_x_sensitive_y:
        value: ${input_x_sensitive + input_y}
        sensitive: true
    - output_input_concat_x_sensitive_y: ${input_concat_x_sensitive_y}
    - output_sensitive_input_concat_x_sensitive_y:
        value: ${input_concat_x_sensitive_y}
        sensitive: true
    - output_concat_x_y_sensitive: ${input_x_sensitive + input_y}
    - output_sensitive_concat_x_y_sensitive:
        value:  ${input_x_sensitive + input_y}
        sensitive: true
    - output_input_concat_x_y_sensitive: ${input_concat_x_y_sensitive}
    - output_sensitive_input_concat_x_y_sensitive:
        value: ${input_concat_x_y_sensitive}
        sensitive: true
    - output_input_concat_x_y_sensitive_sensitive: ${input_concat_x_y_sensitive_sensitive}
    - output_sensitive_input_concat_x_y_sensitive_sensitive:
        value: ${input_concat_x_y_sensitive_sensitive}
        sensitive: true
    - output_concat_x_sensitive_y_sensitive: ${input_x_sensitive + input_y_sensitive}
    - output_sensitive_concat_x_sensitive_y_sensitive:
        value: ${input_x_sensitive + input_y_sensitive}
        sensitive: true
    - output_input_concat_x_sensitive_y_sensitive_sensitive: ${input_concat_x_sensitive_y_sensitive_sensitive}
    - output_sensitive_input_concat_x_sensitive_y_sensitive_sensitive:
        value: ${input_concat_x_sensitive_y_sensitive_sensitive}
        sensitive: true

    - output_concat_x_folded: ${'prefix_' + input_concat_x + '_suffix'}
    - output_sensitive_concat_x_folded:
        value: ${'prefix_' + input_concat_x + '_suffix'}
        sensitive: true
    - output_input_concat_x_folded: ${input_concat_x_folded}
    - output_sensitive_input_concat_x_folded:
        value: ${input_concat_x_folded}
        sensitive: true
    - output_input_concat_x_folded_sensitive: ${input_concat_x_folded_sensitive}
    - output_sensitive_input_concat_x_folded_sensitive:
        value: ${input_concat_x_folded_sensitive}
        sensitive: true
    - output_input_sensitive_concat_x_folded: ${input_sensitive_concat_x_folded}
    - output_sensitive_input_sensitive_concat_x_folded:
        value: ${input_sensitive_concat_x_folded}
        sensitive: true
    - output_input_concat_x_folded_copy: ${input_concat_x_folded_copy}
    - output_sensitive_input_concat_x_folded_copy:
        value: ${input_concat_x_folded_copy}
        sensitive: true
    - output_input_concat_x_folded_copy_sensitive: ${input_concat_x_folded_copy_sensitive}
    - output_sensitive_input_concat_x_folded_copy_sensitive:
        value: ${input_concat_x_folded_copy_sensitive}
        sensitive: true
    - output_input_sensitive_concat_x_folded_copy: ${input_sensitive_concat_x_folded_copy}
    - output_sensitive_input_sensitive_concat_x_folded_copy:
        value: ${input_sensitive_concat_x_folded_copy}
        sensitive: true
    - output_input_sensitive_concat_x_folded_sensitive: ${input_sensitive_concat_x_folded_sensitive}
    - output_sensitive_input_sensitive_concat_x_folded_sensitive:
        value: ${input_sensitive_concat_x_folded_sensitive}
        sensitive: true

    - output_expression_characters: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + ':22 --name test1 -v /data:'}
    - output_sensitive_expression_characters:
        value: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + ':22 --name test1 -v /data:'}
        sensitive: true
    - output_input_expression_characters: ${input_expression_characters}
    - output_sensitive_input_expression_characters:
        value: ${input_expression_characters}
        sensitive: true
    - output_input_expression_characters_sensitive: ${input_expression_characters_sensitive}
    - output_sensitive_input_expression_characters_sensitive:
        value: ${input_expression_characters_sensitive}
        sensitive: true
    - output_input_sensitive_expression_characters: ${input_sensitive_expression_characters}
    - output_sensitive_input_sensitive_expression_characters:
        value: ${input_sensitive_expression_characters}
        sensitive: true
    - output_input_sensitive_expression_characters_sensitive: ${input_sensitive_expression_characters_sensitive}
    - output_sensitive_input_sensitive_expression_characters_sensitive:
        value: ${input_sensitive_expression_characters_sensitive}
        sensitive: true

  results:
    - SUCCESS: ${True}
    - FAILURE
