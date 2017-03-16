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
        default: Hi
        private: false
        sensitive: false
    - input_overridable_hi1_sensitive:
        default: Hi
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
# uncomment when types will be supported
#    - input_yaml_list:
#        default: '[1, 2, 3]'
#    - input_yaml_list_sensitive:
#        default: '[1, 2, 3]'
#        sensitive: true
#
#    - input_yaml_map:
#        default: {'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}
#    - input_yaml_map_sensitive:
#        default: {'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}
#        sensitive: true
#
#    - input_properties_yaml_map_folded:
#        default: {default: medium, required: false}
#    - input_properties_yaml_map_folded_sensitive:
#        default: {default: medium, required: false}
#        sensitive: true
#
#    - input_python_null:
#        default: ${ None }
#        required: false
#    - input_python_null_sensitive:
#        default: ${ None }
#        required: false
#        sensitive: true
#
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
    - input_1_concat_x_sensitive_y_sensitive:
        default: ${input_x_sensitive + input_y}
        sensitive: true
    - input_concat_x_y_sensitive: ${input_x + input_y_sensitive}
    - input_concat_x_y_sensitive_sensitive:
        default: ${input_x + input_y_sensitive}
        sensitive: true
    - input_concat_x_sensitive_y_sensitive: ${input_x_sensitive + input_y_sensitive}
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
    - input_1_sensitive_concat_x_folded_sensitive:
        default: > 
          ${
          'prefix_' +
          input_concat_x_sensitive +
          '_suffix'
          }
        sensitive: true   
        
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

  workflow:
    - get_data:
        do:
          ops.noop:
            - argument_no_value        
            - argument_input_no_value:                                              ${input_no_value}             
#            - argument_input_no_value_sensitive:                                    ${input_no_value_sensitive}

            - argument_input_get_input_no_value:                                    ${input_get_input_no_value}
            - argument_input_get_input_no_value_sensitive:                          ${input_get_input_no_value_sensitive}
            - argument_input_get_sensitive_input_no_value:                          ${input_get_sensitive_input_no_value}
            - argument_input_get_sensitive_input_no_value_sensitive:                ${input_get_sensitive_input_no_value_sensitive}
            
            - argument_python_get_input_no_value:                                    ${get("input_no_value", "default_value")}
            - argument_python_get_sensitive_input_no_value:                         ${get("input_no_value_sensitive", "default_value")}
            - argument_input_python_get_input_no_value:                             ${input_python_get_input_no_value}
            - argument_input_python_get_input_no_value_sensitive:                   ${input_python_get_input_no_value_sensitive}
            - argument_input_python_get_sensitive_input_no_value:                   ${input_python_get_sensitive_input_no_value}
            - argument_input_python_get_sensitive_input_no_value_sensitive:         ${input_python_get_sensitive_input_no_value_sensitive}

            - argument_with_value:                                                  'bbb'
            - argument_input_with_value:                                            ${input_with_value}  
            - argument_input_with_value_sensitive:                                  ${input_with_value_sensitive}  
            
            - argument_input_get_input_with_value:                                  ${input_get_input_with_value}
            - argument_input_get_input_with_value_sensitive:                        ${input_get_input_with_value_sensitive}
            - argument_input_get_sensitive_input_with_value:                        ${input_get_sensitive_input_with_value}
            - argument_input_get_sensitive_input_with_value_sensitive:              ${input_get_sensitive_input_with_value_sensitive}
            
            - argument_python_get_input_with_value:                                 ${get("input_with_value", "default_value")}
            - argument_python_get_input_with_value_sensitive:                       ${get("input_with_value_sensitive", "default_value")}
            - argument_input_python_get_input_with_value:                           ${input_python_get_input_with_value}
            - argument_input_python_get_input_with_value_sensitive:                 ${input_python_get_input_with_value_sensitive}
            - argument_input_python_get_sensitive_input_with_value:                 ${input_python_get_sensitive_input_with_value}
            - argument_input_python_get_sensitive_input_with_value_sensitive:       ${input_python_get_sensitive_input_with_value_sensitive}
            
            - argument_system_property:                                             ${get_sp('user.sys.props.host')}            
            - argument_input_system_property:                                       ${input_system_property}
            - argument_input_system_property_sensitive:                             ${input_system_property_sensitive}

            - argument_get_input_system_property:                                   ${input_system_property}
            - argument_get_input_system_property_sensitive:                         ${input_system_property_sensitive}
            - argument_input_get_input_system_property:                             ${input_get_input_system_property}
            - argument_input_get_input_system_property_sensitive:                   ${input_get_input_system_property_sensitive}
            - argument_input_get_sensitive_input_system_property:                   ${input_get_sensitive_input_system_property}
            - argument_input_get_sensitive_input_system_property_sensitive:         ${input_get_sensitive_input_system_property_sensitive}
            
            - argument_python_get_input_system_property:                            ${get("input_system_property", "default_value")}
            - argument_python_get_input_system_property_sensitive:                  ${get("input_system_property_sensitive", "default_value")}
            - argument_input_python_get_input_system_property:                      ${input_python_get_input_system_property}
            - argument_input_python_get_input_system_property_sensitive:            ${input_python_get_input_system_property_sensitive}
            - argument_input_python_get_sensitive_input_system_property:            ${input_python_get_sensitive_input_system_property}
            - argument_input_python_get_sensitive_input_system_property_sensitive:  ${input_python_get_sensitive_input_system_property_sensitive}

            - argument_authorized_keys_path:                                        './auth'
            - argument_input_authorized_keys_path:                                  ${input_authorized_keys_path}
            - argument_input_authorized_keys_path_sensitive:                        ${input_authorized_keys_path_sensitive}

            - argument_scp_host_port:                                               '9999'
            - argument_input_scp_host_port:                                         ${input_scp_host_port}
            - argument_input_scp_host_port_sensitive:                               ${input_scp_host_port_sensitive}
            
            - argument_input_not_overridable:                                       ${input_not_overridable}
            - argument_input_not_overridable_sensitive:                             ${input_not_overridable_sensitive}
            
            - argument_overridable_hi1:                                             Hi
            - argument_input_overridable_hi1:                                       ${input_overridable_hi1}
            - argument_input_overridable_hi1_sensitive:                             ${input_overridable_hi1_sensitive}

            - argument_overridable_hi2:                                             'Hi'
            - argument_input_overridable_hi2:                                       ${input_overridable_hi2}
            - argument_input_overridable_hi2_sensitive:                             ${input_overridable_hi2_sensitive}

            - argument_overridable_hi3:                                             "Hi"
            - argument_input_overridable_hi3:                                       ${input_overridable_hi3}
            - argument_input_overridable_hi3_sensitive:                             ${input_overridable_hi3_sensitive}

            - argument_yaml_list:                                                   '[1, 2, 3]'
#            - argument_input_yaml_list:                                             ${input_yaml_list}
#            - argument_input_yaml_list_sensitive:                                   ${input_yaml_list_sensitive}
            
#            - argument_yaml_map:                                                    "{'key1': 'value1', 'key2': 'value2', 'key3': 'value3'}"
#            - argument_input_yaml_map:                                              ${input_yaml_map}
#            - argument_input_yaml_map_sensitive:                                    ${input_yaml_map_sensitive}
            
#            - argument_properties_yaml_map_folded:                                  "{default: medium, required: false}"
#            - argument_input_properties_yaml_map_folded:                            ${input_properties_yaml_map_folded}
#            - argument_input_properties_yaml_map_folded_sensitive:                  ${input_properties_yaml_map_folded_sensitive}
            
            - argument_python_null:                                                 ${ None }
#            - argument_input_python_null:                                           ${input_python_null}
#            - argument_input_python_null_sensitive:                                 ${input_python_null_sensitive}

#            - argument_python_list:                                                 ${[1, 2, 3]}
#            - argument_input_python_list:                                           ${input_python_list}
#            - argument_input_python_list_sensitive:                                 ${input_python_list_sensitive}

#            - argument_python_map:                                                  ${{'key1':'value1', 'key2':'value2', 'key3':'value3'}}
#            - argument_input_python_map:                                            ${input_python_map}
#            - argument_input_python_map_sensitive:                                  ${input_python_map_sensitive}

            - argument_x:                                                           x
            - argument_input_x:                                                     ${input_x}
            - argument_input_x_sensitive:                                           ${input_x_sensitive}
            - argument_input_x_copy:                                                ${input_x_copy}
            - argument_input_x_copy_sensitive:                                      ${input_x_copy_sensitive} 
            - argument_input_sensitive_x_copy:                                      ${input_sensitive_x_copy} 
            - argument_input_sensitive_x_copy_sensitive:                            ${input_sensitive_x_copy_sensitive} 

            - argument_concat_x:                                                    ${'a' + input_x}
            - argument_input_concat_x:                                              ${input_concat_x}
            - argument_input_concat_x_sensitive:                                    ${input_concat_x_sensitive} 
            - argument_input_sensitive_concat_x:                                    ${input_sensitive_concat_x}
            - argument_input_sensitive_concat_x_sensitive:                          ${input_sensitive_concat_x_sensitive}                         

            - argument_y:                                                           y
            - argument_concat_y:                                                    ${input_y + "b"}
            - argument_input_concat_y:                                              ${input_concat_y}
            - argument_input_concat_y_sensitive:                                    ${input_concat_y_sensitive}
            - argument_input_sensitive_concat_y:                                    ${input_sensitive_concat_y}
            - argument_input_sensitive_concat_y_sensitive:                          ${input_sensitive_concat_y_sensitive}

            - argument_concat_ab:                                                   ${'a' + 'b'}
            - argument_input_concat_ab:                                             ${input_concat_ab}
            - argument_input_concat_ab_sensitive:                                   ${input_concat_ab_sensitive}

            - argument_concat_xy:                                                   ${input_x + input_y}
            - argument_input_concat_xy:                                             ${input_concat_xy}
            - argument_input_concat_xy_sensitive:                                   ${input_concat_xy_sensitive}
            - argument_concat_x_sensitive_y:                                        ${input_x_sensitive + input_y}
            - argument_input_concat_x_sensitive_y:                                  ${input_concat_x_sensitive_y}
            - argument_1_input_concat_x_sensitive_y_sensitive:                      ${input_concat_x_sensitive_y_sensitive}
            - argument_concat_x_y_sensitive:                                        ${input_x_sensitive + input_y}
            - argument_input_concat_x_y_sensitive:                                  ${input_concat_x_y_sensitive}
            - argument_input_concat_x_y_sensitive_sensitive:                        ${input_concat_x_y_sensitive_sensitive}
            - argument_concat_x_sensitive_y_sensitive:                              ${input_x_sensitive + input_y_sensitive}
            - argument_input_concat_x_sensitive_y_sensitive:                      ${input_concat_x_sensitive_y_sensitive}
            - argument_input_concat_x_sensitive_y_sensitive_sensitive:              ${input_concat_x_sensitive_y_sensitive_sensitive}

            - argument_concat_x_folded:                                             ${'prefix_' + input_concat_x + '_suffix'}
            - argument_input_concat_x_folded:                                       ${input_concat_x_folded} 
            - argument_input_concat_x_folded_sensitive:                             ${input_concat_x_folded_sensitive} 
            - argument_input_sensitive_concat_x_folded:                             ${input_sensitive_concat_x_folded} 
            - argument_input_sensitive_concat_x_folded_sensitive:                   ${input_sensitive_concat_x_folded_sensitive}            
            - argument_input_concat_x_folded_copy:                                  ${input_concat_x_folded_copy} 
            - argument_input_concat_x_folded_copy_sensitive:                        ${input_concat_x_folded_copy_sensitive} 
            - argument_input_sensitive_concat_x_folded_copy:                        ${input_sensitive_concat_x_folded_copy} 
            - argument_expression_characters:                                       ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + ':22 --name test1 -v /data:'}
            - argument_input_expression_characters:                                 ${input_expression_characters}
            - argument_input_expression_characters_sensitive:                       ${input_expression_characters_sensitive}
            - argument_input_sensitive_expression_characters:                       ${input_sensitive_expression_characters}
            - argument_input_sensitive_expression_characters_sensitive:             ${input_sensitive_expression_characters_sensitive}

#        publish:
#          - argument_no_value
#          - argument_input_no_value
#          - argument_input_no_value_sensitive
#          - argument_input_get_input_no_value
#          - argument_input_get_input_no_value_sensitive
#          - argument_input_get_sensitive_input_no_value
#          - argument_input_get_sensitive_input_no_value_sensitive
#          - argument_python_get_input_no_value
#          - argument_python_get_sensitive_input_no_value
#          - argument_input_python_get_input_no_value
#          - argument_input_python_get_input_no_value_sensitive
#          - argument_input_python_get_sensitive_input_no_value
#          - argument_input_python_get_sensitive_input_no_value_sensitive
#          - argument_with_value
#          - argument_input_with_value
#          - argument_input_with_value_sensitive
#          - argument_input_get_input_with_value
#          - argument_input_get_input_with_value_sensitive
#          - argument_input_get_sensitive_input_with_value
#          - argument_input_get_sensitive_input_with_value_sensitive
#          - argument_python_get_input_with_value
#          - argument_python_get_input_with_value_sensitive
#          - argument_input_python_get_input_with_value
#          - argument_input_python_get_input_with_value_sensitive
#          - argument_input_python_get_sensitive_input_with_value
#          - argument_input_python_get_sensitive_input_with_value_sensitive
#          - argument_system_property
#          - argument_input_system_property
#          - argument_input_system_property_sensitive
#          - argument_get_input_system_property
#          - argument_get_input_system_property_sensitive
#          - argument_input_get_input_system_property
#          - argument_input_get_input_system_property_sensitive
#          - argument_input_get_sensitive_input_system_property
#          - argument_input_get_sensitive_input_system_property_sensitive
#          - argument_python_get_input_system_property
#          - argument_python_get_input_system_property_sensitive
#          - argument_input_python_get_input_system_property
#          - argument_input_python_get_input_system_property_sensitive
#          - argument_input_python_get_sensitive_input_system_property
#          - argument_input_python_get_sensitive_input_system_property_sensitive
#          - argument_authorized_keys_path
#          - argument_input_authorized_keys_path
#          - argument_input_authorized_keys_path_sensitive
#          - argument_scp_host_port
#          - argument_input_scp_host_port
#          - argument_input_scp_host_port_sensitive
#          - argument_input_not_overridable
#          - argument_input_not_overridable_sensitive
#          - argument_overridable_hi1
#          - argument_input_overridable_hi1
#          - argument_input_overridable_hi1_sensitive
#          - argument_overridable_hi2
#          - argument_input_overridable_hi2
#          - argument_input_overridable_hi2_sensitive
#          - argument_overridable_hi3
#          - argument_input_overridable_hi3
#          - argument_input_overridable_hi3_sensitive
#          - argument_yaml_list
#          - argument_input_yaml_list
#          - argument_input_yaml_list_sensitive
#          - argument_yaml_map
#          - argument_input_yaml_map
#          - argument_input_yaml_map_sensitive
#          - argument_properties_yaml_map_folded
#          - argument_input_properties_yaml_map_folded
#          - argument_input_properties_yaml_map_folded_sensitive
#          - argument_python_null
#          - argument_input_python_null
#          - argument_input_python_null_sensitive
#          - argument_python_list
#          - argument_input_python_list
#          - argument_input_python_list_sensitive
#          - argument_python_map
#          - argument_input_python_map
#          - argument_input_python_map_sensitive
#          - argument_x
#          - argument_input_x
#          - argument_input_x_sensitive
#          - argument_input_x_copy
#          - argument_input_x_copy_sensitive
#          - argument_input_sensitive_x_copy
#          - argument_input_sensitive_x_copy_sensitive
#          - argument_concat_x
#          - argument_input_concat_x
#          - argument_input_concat_x_sensitive
#          - argument_input_sensitive_concat_x
#          - argument_input_sensitive_concat_x_sensitive
#          - argument_y
#          - argument_concat_y
#          - argument_input_concat_y
#          - argument_input_concat_y_sensitive
#          - argument_input_sensitive_concat_y
#          - argument_input_sensitive_concat_y_sensitive
#          - argument_concat_ab
#          - argument_input_concat_ab
#          - argument_input_concat_ab_sensitive
#          - argument_concat_xy
#          - argument_input_concat_xy
#          - argument_input_concat_xy_sensitive
#          - argument_concat_x_sensitive_y
#          - argument_input_concat_x_sensitive_y
#          - argument_input_concat_x_sensitive_y_sensitive
#          - argument_concat_x_y_sensitive
#          - argument_input_concat_x_y_sensitive
#          - argument_input_concat_x_y_sensitive_sensitive
#          - argument_concat_x_sensitive_y_sensitive
#          - argument_input_concat_x_sensitive_y_sensitive_sensitive
#          - argument_concat_x_folded
#          - argument_input_concat_x_folded
#          - argument_input_concat_x_folded_sensitive
#          - argument_input_sensitive_concat_x_folded
#          - argument_input_sensitive_concat_x_folded_sensitive
#          - argument_input_concat_x_folded_copy
#          - argument_input_concat_x_folded_copy_sensitive
#          - argument_input_sensitive_concat_x_folded_copy
#          - argument_expression_characters
#          - argument_input_expression_characters
#          - argument_input_expression_characters_sensitive
#          - argument_input_sensitive_expression_characters
#          - argument_input_sensitive_expression_characters_sensitive
        navigate:
          - SUCCESS: prepare_for_print

    - prepare_for_print:
        do:
          ops.noop:
#            - argument_python_all_values_list: >
#                ${[argument_input_python_get_input_no_value, argument_input_python_get_input_no_value_sensitive, argument_input_python_get_sensitive_input_no_value,
#                argument_input_python_get_sensitive_input_no_value_sensitive, argument_with_value, argument_input_with_value, argument_input_with_value_sensitive,
#                argument_input_get_input_with_value, argument_input_get_input_with_value_sensitive, argument_input_get_sensitive_input_with_value,
#                argument_input_get_sensitive_input_with_value_sensitive, argument_python_get_input_with_value, argument_python_get_input_with_value_sensitive,
#                argument_input_python_get_input_with_value, argument_input_python_get_input_with_value_sensitive, argument_input_python_get_sensitive_input_with_value,
#                argument_input_python_get_sensitive_input_with_value_sensitive, argument_system_property, argument_input_system_property,
#                argument_input_system_property_sensitive, argument_get_input_system_property, argument_get_input_system_property_sensitive,
#                argument_input_get_input_system_property, argument_input_get_input_system_property_sensitive, argument_input_get_sensitive_input_system_property,
#                argument_input_get_sensitive_input_system_property_sensitive, argument_python_get_input_system_property, argument_python_get_input_system_property_sensitive,
#                argument_input_python_get_input_system_property, argument_input_python_get_input_system_property_sensitive,
#                argument_input_python_get_sensitive_input_system_property, argument_input_python_get_sensitive_input_system_property_sensitive, argument_authorized_keys_path,
#                argument_input_authorized_keys_path, argument_input_authorized_keys_path_sensitive, argument_scp_host_port, argument_input_scp_host_port,
#                argument_input_scp_host_port_sensitive, argument_input_not_overridable, argument_input_not_overridable_sensitive, argument_overridable_hi1,
#                argument_input_overridable_hi1, argument_input_overridable_hi1_sensitive, argument_overridable_hi2, argument_input_overridable_hi2,
#                argument_input_overridable_hi2_sensitive, argument_overridable_hi3, argument_input_overridable_hi3, argument_input_overridable_hi3_sensitive,
#                argument_yaml_list, argument_input_yaml_list, argument_input_yaml_list_sensitive, argument_yaml_map, argument_input_yaml_map,
#                argument_input_yaml_map_sensitive, argument_properties_yaml_map_folded, argument_input_properties_yaml_map_folded,
#                argument_input_properties_yaml_map_folded_sensitive, argument_python_list, argument_input_python_list, argument_input_python_list_sensitive, argument_python_map,
#                argument_input_python_map, argument_input_python_map_sensitive, argument_x, argument_input_x, argument_input_x_sensitive, argument_input_x_copy,
#                argument_input_x_copy_sensitive, argument_input_sensitive_x_copy, argument_input_sensitive_x_copy_sensitive, argument_concat_x, argument_input_concat_x,
#                argument_input_concat_x_sensitive, argument_input_sensitive_concat_x, argument_input_sensitive_concat_x_sensitive, argument_y, argument_concat_y,
#                argument_input_concat_y, argument_input_concat_y_sensitive, argument_input_sensitive_concat_y, argument_input_sensitive_concat_y_sensitive, argument_concat_ab,
#                argument_input_concat_ab, argument_input_concat_ab_sensitive, argument_concat_xy, argument_input_concat_xy, argument_input_concat_xy_sensitive,
#                argument_concat_x_sensitive_y, argument_input_concat_x_sensitive_y, argument_input_concat_x_sensitive_y_sensitive, argument_concat_x_y_sensitive,
#                argument_input_concat_x_y_sensitive, argument_input_concat_x_y_sensitive_sensitive, argument_concat_x_sensitive_y_sensitive,
#                argument_input_concat_x_sensitive_y_sensitive, argument_input_concat_x_sensitive_y_sensitive_sensitive, argument_concat_x_folded,
#                argument_input_concat_x_folded, argument_input_concat_x_folded_sensitive, argument_input_sensitive_concat_x_folded,
#                argument_input_sensitive_concat_x_folded_sensitive, argument_input_concat_x_folded_copy, argument_input_concat_x_folded_copy_sensitive,
#                argument_input_sensitive_concat_x_folded_copy, argument_input_sensitive_concat_x_folded_sensitive, argument_expression_characters,
#                argument_input_expression_characters, argument_input_expression_characters_sensitive, argument_input_sensitive_expression_characters,
#                argument_input_sensitive_expression_characters_sensitive]}
#
#        publish:
#          - argument_python_all_values_list
        navigate:
          - SUCCESS: SUCCESS
#
#    - print_values:
#        loop:
#          for: value in argument_python_all_values_list
#          do:
#            ops.print:
#              - text: ${ value }
#          break: []
#          navigate:
#            - SUCCESS: SUCCESS

#  outputs:
#    - argument_no_value
#    - argument_input_no_value
#    - argument_input_no_value_sensitive
#    - argument_input_get_input_no_value
#    - argument_input_get_input_no_value_sensitive
#    - argument_input_get_sensitive_input_no_value
#    - argument_input_get_sensitive_input_no_value_sensitive
#    - argument_python_get_input_no_value
#    - argument_python_get_sensitive_input_no_value
#    - argument_input_python_get_input_no_value
#    - argument_input_python_get_input_no_value_sensitive
#    - argument_input_python_get_sensitive_input_no_value
#    - argument_input_python_get_sensitive_input_no_value_sensitive
#    - argument_with_value
#    - argument_input_with_value
#    - argument_input_with_value_sensitive
#    - argument_input_get_input_with_value
#    - argument_input_get_input_with_value_sensitive
#    - argument_input_get_sensitive_input_with_value
#    - argument_input_get_sensitive_input_with_value_sensitive
#    - argument_python_get_input_with_value
#    - argument_python_get_input_with_value_sensitive
#    - argument_input_python_get_input_with_value
#    - argument_input_python_get_input_with_value_sensitive
#    - argument_input_python_get_sensitive_input_with_value
#    - argument_input_python_get_sensitive_input_with_value_sensitive
#    - argument_system_property
#    - argument_input_system_property
#    - argument_input_system_property_sensitive
#    - argument_get_input_system_property
#    - argument_get_input_system_property_sensitive
#    - argument_input_get_input_system_property
#    - argument_input_get_input_system_property_sensitive
#    - argument_input_get_sensitive_input_system_property
#    - argument_input_get_sensitive_input_system_property_sensitive
#    - argument_python_get_input_system_property
#    - argument_python_get_input_system_property_sensitive
#    - argument_input_python_get_input_system_property
#    - argument_input_python_get_input_system_property_sensitive
#    - argument_input_python_get_sensitive_input_system_property
#    - argument_input_python_get_sensitive_input_system_property_sensitive
#    - argument_authorized_keys_path
#    - argument_input_authorized_keys_path
#    - argument_input_authorized_keys_path_sensitive
#    - argument_scp_host_port
#    - argument_input_scp_host_port
#    - argument_input_scp_host_port_sensitive
#    - argument_input_not_overridable
#    - argument_input_not_overridable_sensitive
#    - argument_overridable_hi1
#    - argument_input_overridable_hi1
#    - argument_input_overridable_hi1_sensitive
#    - argument_overridable_hi2
#    - argument_input_overridable_hi2
#    - argument_input_overridable_hi2_sensitive
#    - argument_overridable_hi3
#    - argument_input_overridable_hi3
#    - argument_input_overridable_hi3_sensitive
#    - argument_yaml_list
#    - argument_input_yaml_list
#    - argument_input_yaml_list_sensitive
#    - argument_yaml_map
#    - argument_input_yaml_map
#    - argument_input_yaml_map_sensitive
#    - argument_properties_yaml_map_folded
#    - argument_input_properties_yaml_map_folded
#    - argument_input_properties_yaml_map_folded_sensitive
#    - argument_python_null
#    - argument_input_python_null
#    - argument_input_python_null_sensitive
#    - argument_python_list
#    - argument_input_python_list
#    - argument_input_python_list_sensitive
#    - argument_python_map
#    - argument_input_python_map
#    - argument_input_python_map_sensitive
#    - argument_x
#    - argument_input_x
#    - argument_input_x_sensitive
#    - argument_input_x_copy
#    - argument_input_x_copy_sensitive
#    - argument_input_sensitive_x_copy
#    - argument_input_sensitive_x_copy_sensitive
#    - argument_concat_x
#    - argument_input_concat_x
#    - argument_input_concat_x_sensitive
#    - argument_input_sensitive_concat_x
#    - argument_input_sensitive_concat_x_sensitive
#    - argument_y
#    - argument_concat_y
#    - argument_input_concat_y
#    - argument_input_concat_y_sensitive
#    - argument_input_sensitive_concat_y
#    - argument_input_sensitive_concat_y_sensitive
#    - argument_concat_ab
#    - argument_input_concat_ab
#    - argument_input_concat_ab_sensitive
#    - argument_concat_xy
#    - argument_input_concat_xy
#    - argument_input_concat_xy_sensitive
#    - argument_concat_x_sensitive_y
#    - argument_input_concat_x_sensitive_y
#    - argument_concat_x_y_sensitive
#    - argument_input_concat_x_y_sensitive
#    - argument_input_concat_x_y_sensitive_sensitive
#    - argument_concat_x_sensitive_y_sensitive
#    - argument_input_concat_x_sensitive_y_sensitive
#    - argument_input_concat_x_sensitive_y_sensitive_sensitive
#    - argument_concat_x_folded
#    - argument_input_concat_x_folded
#    - argument_input_concat_x_folded_sensitive
#    - argument_input_sensitive_concat_x_folded
#    - argument_input_concat_x_folded_copy
#    - argument_input_concat_x_folded_copy_sensitive
#    - argument_input_sensitive_concat_x_folded_copy
#    - argument_input_sensitive_concat_x_folded_sensitive
#    - argument_expression_characters
#    - argument_input_expression_characters
#    - argument_input_expression_characters_sensitive
#    - argument_input_sensitive_expression_characters
#    - argument_input_sensitive_expression_characters_sensitive
#    - argument_all_values
#
  results:
    - SUCCESS
