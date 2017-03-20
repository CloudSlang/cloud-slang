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
  name: sensitive_values_flow_step_input_modifiers  
  inputs:
    - input_no_value:  
         required: false
         sensitive: false
    - input_no_value_placeholder:  
         required: false
         sensitive: false

    - input_get_input_no_value:
        default: ${input_no_value}
        required: false
        private: true
    - input_get_input_no_value_placeholder:
        default: ${input_no_value}
        required: false
        private: true
        sensitive: false
    - input_get_placeholder_input_no_value:
        default: ${input_no_value_placeholder}
        required: false
    - input_get_placeholder_input_no_value_placeholder:
        default: ${input_no_value_placeholder}
        required: false
        sensitive: false

    - input_python_get_input_no_value:
        default: ${get("input_no_value", "default_value")}
        private: true
    - input_python_get_input_no_value_placeholder:
        default: ${get("input_no_value", "default_value")}
        private: true
        sensitive: false
    - input_python_get_placeholder_input_no_value:
        default: ${get("input_no_value_placeholder", "default_value")}
    - input_python_get_placeholder_input_no_value_placeholder:
        default: ${get("input_no_value_placeholder", "default_value")}
        sensitive: false

    - input_with_value:  
        default: 'aaa'
    - input_with_value_placeholder:  
        default: 'aaa'
        sensitive: false

    - input_get_input_with_value:
        default: ${input_with_value}
        private: true
    - input_get_input_with_value_placeholder:
        default: ${input_with_value}
        private: true
        sensitive: false
    - input_get_placeholder_input_with_value:
        default: ${input_with_value_placeholder}
    - input_get_placeholder_input_with_value_placeholder:
        default: ${input_with_value_placeholder}
        sensitive: false
   
    - input_python_get_input_with_value:
        default: ${get("input_with_value", "default_value")}
        private: true
    - input_python_get_input_with_value_placeholder:
        default: ${get("input_with_value", "default_value")}
        private: true
        sensitive: false
    - input_python_get_placeholder_input_with_value:
        default: ${get("input_with_value_placeholder", "default_value")}
    - input_python_get_placeholder_input_with_value_placeholder:
        default: ${get("input_with_value_placeholder", "default_value")}
        sensitive: false
   
    - input_system_property:
        default: ${get_sp('user.sys.props.host')}
    - input_system_property_placeholder:
        default: ${get_sp('user.sys.props.host')}
        sensitive: false

    - input_get_input_system_property:
        default: ${input_system_property}
    - input_get_input_system_property_placeholder:
        default: ${input_system_property}
        sensitive: false
    - input_get_placeholder_input_system_property:
        default: ${input_system_property_placeholder}
    - input_get_placeholder_input_system_property_placeholder:
        default: ${input_system_property_placeholder}
        sensitive: false

    - input_python_get_input_system_property:
        default: ${get("input_system_property", "default_value")}
    - input_python_get_input_system_property_placeholder:
        default: ${get("input_system_property", "default_value")}
        sensitive: false
    - input_python_get_placeholder_input_system_property:
        default: ${get("input_system_property_placeholder", "default_value")}
    - input_python_get_placeholder_input_system_property_placeholder:
        default: ${get("input_system_property_placeholder", "default_value")}
        sensitive: false
        
    - input_authorized_keys_path:
        default: './auth'
        sensitive: false
    - input_authorized_keys_path_placeholder:
        default: './auth'
        sensitive: false
        
    - input_scp_host_port:
        default: '8888'
    - input_scp_host_port_placeholder:
        default: '8888'
        sensitive: false
        
    - input_not_overridable:
        default: '10'
        private: true
        sensitive: false
    - input_not_overridable_placeholder:
        default: '10'
        private: true
        sensitive: false

    - input_overridable_hi1:
        default: Hi
        private: false
        sensitive: false
    - input_overridable_hi1_placeholder:
        default: Hi
        private: false
        sensitive: false

    - input_overridable_hi2:
        default: 'Hi'
        private: false
        sensitive: false
    - input_overridable_hi2_placeholder:
        default: 'Hi'
        private: false
        sensitive: false

    - input_overridable_hi3:
        default: "Hi"
        private: false
        sensitive: false
    - input_overridable_hi3_placeholder:
        default: "Hi"
        private: false
        sensitive: false

    - input_x: x
    - input_x_placeholder:
        default: x
        sensitive: false
        
    - input_x_copy: ${ input_x }
    - input_x_copy_placeholder: 
        default: ${ input_x }    
        sensitive: false        
    - input_placeholder_x_copy: 
        default: ${ input_x_placeholder }
    - input_placeholder_x_copy_placeholder: 
        default: ${ input_x_placeholder }    
        sensitive: false
        
    - input_concat_x: ${'a' + input_x}
    - input_concat_x_placeholder:
        default: ${'a' + input_x}
        sensitive: false
    - input_placeholder_concat_x: ${'a' + input_x_placeholder}
    - input_placeholder_concat_x_placeholder:
        default: ${'a' + input_x_placeholder}
        sensitive: false

    - input_y: 'y'
    - input_y_placeholder:
        default: 'y'
        sensitive: false

    - input_concat_y: ${input_y + "b"}
    - input_concat_y_placeholder:
        default: ${input_y + "b"}
        sensitive: false
    - input_placeholder_concat_y: ${input_y_placeholder + "b"}
    - input_placeholder_concat_y_placeholder:
        default: ${input_y_placeholder + "b"}
        sensitive: false

    - input_concat_ab: ${'a' + 'b'}
    - input_concat_ab_placeholder:
        default: ${'a' + 'b'}
        sensitive: false

    - input_concat_xy: ${input_x + input_y}
    - input_concat_xy_placeholder:
        default: ${input_x + input_y}
        sensitive: false
    - input_concat_x_placeholder_y: ${input_x_placeholder + input_y}
    - input_1_concat_x_placeholder_y_placeholder:
        default: ${input_x_placeholder + input_y}
        sensitive: false
    - input_concat_x_y_placeholder: ${input_x + input_y_placeholder}
    - input_concat_x_y_placeholder_placeholder:
        default: ${input_x + input_y_placeholder}
        sensitive: false
    - input_concat_x_placeholder_y_placeholder: ${input_x_placeholder + input_y_placeholder}
    - input_concat_x_placeholder_y_placeholder_placeholder:
        default: ${input_x_placeholder + input_y_placeholder}
        sensitive: false

    - input_concat_x_folded: >
        ${
        'prefix_' +
        input_concat_x +
        '_suffix'
        }
    - input_concat_x_folded_placeholder: 
        default: > 
          ${
          'prefix_' +
          input_concat_x +
          '_suffix'
          }
        sensitive: false   
    - input_placeholder_concat_x_folded: >
        ${
        'prefix_' +
        input_concat_x_placeholder +
        '_suffix'
        }
    - input_1_placeholder_concat_x_folded_placeholder:
        default: > 
          ${
          'prefix_' +
          input_concat_x_placeholder +
          '_suffix'
          }
        sensitive: false   
        
    - input_concat_x_folded_copy: 
        default: ${ input_concat_x_folded }
    - input_concat_x_folded_copy_placeholder: 
        default: ${ input_concat_x_folded }    
        sensitive: false        
    - input_placeholder_concat_x_folded_copy: 
        default: ${ input_concat_x_folded_placeholder }
    - input_placeholder_concat_x_folded_placeholder: 
        default: ${ input_concat_x_folded_placeholder }    
        sensitive: false        
        
    - input_expression_characters:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + ':22 --name test1 -v /data:'}
    - input_expression_characters_placeholder:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + ':22 --name test1 -v /data:'}
        sensitive: false
    - input_placeholder_expression_characters:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path_placeholder + '} -p ' + input_scp_host_port_placeholder + ':22 --name test1 -v /data:'}
    - input_placeholder_expression_characters_placeholder:
        default: ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path_placeholder + '} -p ' + input_scp_host_port_placeholder + ':22 --name test1 -v /data:'}
        sensitive: false

  workflow:
    - get_data:
        do:
          ops.noop:
            - argument_no_value
            - argument_input_no_value_sensitive:
                value: ${input_no_value}
                sensitive: true

            - argument_input_get_input_no_value_sensitive:
               value:                                 ${input_get_input_no_value}
               sensitive: true
            - argument_input_get_input_no_value_placeholder_sensitive:
               value:                       ${input_get_input_no_value_placeholder}
               sensitive: true
            - argument_input_get_placeholder_input_no_value_sensitive:
                value:                      ${input_get_placeholder_input_no_value}
                sensitive: true
            - argument_input_get_placeholder_input_no_value_placeholder_sensitive:
                value:              ${input_get_placeholder_input_no_value_placeholder}
                sensitive: true
            
            - argument_python_get_input_no_value_sensitive:
                value: ${get("input_no_value", "default_value")}
                sensitive: true
            - argument_python_get_placeholder_input_no_value_sensitive:
                value:                         ${get("input_no_value_placeholder", "default_value")}
                sensitive: true
            - argument_input_python_get_input_no_value_sensitive:
                value:                             ${input_python_get_input_no_value}
                sensitive: true
            - argument_input_python_get_input_no_value_placeholder_sensitive:
                value:                   ${input_python_get_input_no_value_placeholder}
                sensitive: true
            - argument_input_python_get_placeholder_input_no_value_sensitive:
                value:                   ${input_python_get_placeholder_input_no_value}
                sensitive: true
            - argument_input_python_get_placeholder_input_no_value_placeholder_sensitive:
                value:         ${input_python_get_placeholder_input_no_value_placeholder}
                sensitive: true

            - argument_with_value_sensitive:
                value:                                                  'bbb'
                sensitive: true
            - argument_input_with_value_sensitive:
                value:                                            ${input_with_value}
                sensitive: true
            - argument_input_with_value_placeholder_sensitive:
                value:                                  ${input_with_value_placeholder}
                sensitive: true
            
            - argument_input_get_input_with_value_sensitive:
                value:                                  ${input_get_input_with_value}
                sensitive: true
            - argument_input_get_input_with_value_placeholder_sensitive:
                value:                        ${input_get_input_with_value_placeholder}
                sensitive: true
            - argument_input_get_placeholder_input_with_value_sensitive:
                value:                        ${input_get_placeholder_input_with_value}
                sensitive: true
            - argument_input_get_placeholder_input_with_value_placeholder_sensitive:
                value:              ${input_get_placeholder_input_with_value_placeholder}
                sensitive: true
            
            - argument_python_get_input_with_value_sensitive:
                value:                                 ${get("input_with_value", "default_value")}
                sensitive: true
            - argument_python_get_input_with_value_placeholder_sensitive:
                value:                      ${get("input_with_value_placeholder", "default_value")}
                sensitive: true
            - argument_input_python_get_input_with_value_sensitive:
                value:                           ${input_python_get_input_with_value}
                sensitive: true
            - argument_input_python_get_input_with_value_placeholder_sensitive:
                value:                 ${input_python_get_input_with_value_placeholder}
                sensitive: true
            - argument_input_python_get_placeholder_input_with_value_sensitive:
                value:                 ${input_python_get_placeholder_input_with_value}
                sensitive: true
            - argument_input_python_get_placeholder_input_with_value_placeholder_sensitive:
                value:       ${input_python_get_placeholder_input_with_value_placeholder}
                sensitive: true

            - argument_system_property_sensitive:
                value:                                             ${get_sp('user.sys.props.host')}
                sensitive: true
            - argument_input_system_property_sensitive:
                value:                                       ${input_system_property}
                sensitive: true
            - argument_input_system_property_placeholder_sensitive:
                value:                             ${input_system_property_placeholder}
                sensitive: true

            - argument_get_input_system_property_sensitive:
                value:                                   ${input_system_property}
                sensitive: true
            - argument_get_input_system_property_placeholder_sensitive:
                value:                         ${input_system_property_placeholder}
                sensitive: true
            - argument_input_get_input_system_property_sensitive:
                value:                             ${input_get_input_system_property}
                sensitive: true
            - argument_input_get_input_system_property_placeholder_sensitive:
                value:                   ${input_get_input_system_property_placeholder}
                sensitive: true
            - argument_input_get_placeholder_input_system_property_sensitive:
                value:                   ${input_get_placeholder_input_system_property}
                sensitive: true
            - argument_input_get_placeholder_input_system_property_placeholder_sensitive:
                value:         ${input_get_placeholder_input_system_property_placeholder}
                sensitive: true

            - argument_python_get_input_system_property_sensitive:
                value:                            ${get("input_system_property", "default_value")}
                sensitive: true
            - argument_python_get_input_system_property_placeholder_sensitive:
                value:                  ${get("input_system_property_placeholder", "default_value")}
                sensitive: true
            - argument_input_python_get_input_system_property_sensitive:
                value:                      ${input_python_get_input_system_property}
                sensitive: true
            - argument_input_python_get_input_system_property_placeholder_sensitive:
                value:            ${input_python_get_input_system_property_placeholder}
                sensitive: true
            - argument_input_python_get_placeholder_input_system_property_sensitive:
                value:            ${input_python_get_placeholder_input_system_property}
                sensitive: true
            - argument_input_python_get_placeholder_input_system_property_placeholder_sensitive:
                value:  ${input_python_get_placeholder_input_system_property_placeholder}
                sensitive: true

            - argument_authorized_keys_path_sensitive:
                value:                                        './auth'
                sensitive: true
            - argument_input_authorized_keys_path_sensitive:
                value:                                  ${input_authorized_keys_path}
                sensitive: true
            - argument_input_authorized_keys_path_placeholder_sensitive:
                value:                        ${input_authorized_keys_path_placeholder}
                sensitive: true

            - argument_scp_host_port_sensitive:
                value:                                               '9999'
                sensitive: true
            - argument_input_scp_host_port_sensitive:
                value:                                         ${input_scp_host_port}
                sensitive: true
            - argument_input_scp_host_port_placeholder_sensitive:
                value:                               ${input_scp_host_port_placeholder}
                sensitive: true

            - argument_input_not_overridable_sensitive:
                value:                                       ${input_not_overridable}
                sensitive: true
            - argument_input_not_overridable_placeholder_sensitive:
                value:                             ${input_not_overridable_placeholder}
                sensitive: true

            - argument_overridable_hi1_sensitive:
                value:                                             Hi
                sensitive: true
            - argument_input_overridable_hi1_sensitive:
                value:                                       ${input_overridable_hi1}
                sensitive: true
            - argument_input_overridable_hi1_placeholder_sensitive:
                value:                             ${input_overridable_hi1_placeholder}
                sensitive: true

            - argument_overridable_hi2_sensitive:
                value:                                             'Hi'
                sensitive: true
            - argument_input_overridable_hi2_sensitive:
                value:                                       ${input_overridable_hi2}
                sensitive: true
            - argument_input_overridable_hi2_placeholder_sensitive:
                value:                             ${input_overridable_hi2_placeholder}
                sensitive: true

            - argument_overridable_hi3_sensitive:
                value:                                             "Hi"
                sensitive: true
            - argument_input_overridable_hi3_sensitive:
                value:                                       ${input_overridable_hi3}
                sensitive: true
            - argument_input_overridable_hi3_placeholder_sensitive:
                value:                             ${input_overridable_hi3_placeholder}
                sensitive: true

            - argument_yaml_list_sensitive:
                value:                                                   '[1, 2, 3]'
                sensitive: true

            - argument_python_null_sensitive:
                value:                                                 ${ None }
                sensitive: true

            - argument_x_sensitive:
                value:                                                           x
                sensitive: true
            - argument_input_x_sensitive:
                value:                                                     ${input_x}
                sensitive: true
            - argument_input_x_placeholder_sensitive:
                value:                                           ${input_x_placeholder}
                sensitive: true
            - argument_input_x_copy_sensitive:
                value:                                                ${input_x_copy}
                sensitive: true
            - argument_input_x_copy_placeholder_sensitive:
                value:                                      ${input_x_copy_placeholder}
                sensitive: true
            - argument_input_placeholder_x_copy_sensitive:
                value:                                      ${input_placeholder_x_copy}
                sensitive: true
            - argument_input_placeholder_x_copy_placeholder_sensitive:
                value:                            ${input_placeholder_x_copy_placeholder}
                sensitive: true

            - argument_concat_x_sensitive:
                value:                                                    ${'a' + input_x}
                sensitive: true
            - argument_input_concat_x_sensitive:
                value:                                              ${input_concat_x}
                sensitive: true
            - argument_input_concat_x_placeholder_sensitive:
                value:                                    ${input_concat_x_placeholder}
                sensitive: true
            - argument_input_placeholder_concat_x_sensitive:
                value:                                    ${input_placeholder_concat_x}
                sensitive: true
            - argument_input_placeholder_concat_x_placeholder_sensitive:
                value:                          ${input_placeholder_concat_x_placeholder}
                sensitive: true

            - argument_y_sensitive:
                value:                                                           y
                sensitive: true
            - argument_concat_y_sensitive:
                value:                                                    ${input_y + "b"}
                sensitive: true
            - argument_input_concat_y_sensitive:
                value:                                              ${input_concat_y}
                sensitive: true
            - argument_input_concat_y_placeholder_sensitive:
                value:                                    ${input_concat_y_placeholder}
                sensitive: true
            - argument_input_placeholder_concat_y_sensitive:
                value:                                    ${input_placeholder_concat_y}
                sensitive: true
            - argument_input_placeholder_concat_y_placeholder_sensitive:
                value:                          ${input_placeholder_concat_y_placeholder}
                sensitive: true

            - argument_concat_ab_sensitive:
                value:                                                   ${'a' + 'b'}
                sensitive: true
            - argument_input_concat_ab_sensitive:
                value:                                             ${input_concat_ab}
                sensitive: true
            - argument_input_concat_ab_placeholder_sensitive:
                value:                                   ${input_concat_ab_placeholder}
                sensitive: true

            - argument_concat_xy_sensitive:
                value:                                                   ${input_x + input_y}
                sensitive: true
            - argument_input_concat_xy_sensitive:
                value:                                             ${input_concat_xy}
                sensitive: true
            - argument_input_concat_xy_placeholder_sensitive:
                value:                                   ${input_concat_xy_placeholder}
                sensitive: true
            - argument_concat_x_placeholder_y_sensitive:
                value:                                        ${input_x_placeholder + input_y}
                sensitive: true
            - argument_input_concat_x_placeholder_y_sensitive:
                value:                                  ${input_concat_x_placeholder_y}
                sensitive: true
            - argument_1_input_concat_x_placeholder_y_placeholder_sensitive:
                value:                      ${input_concat_x_placeholder_y_placeholder}
                sensitive: true
            - argument_concat_x_y_placeholder_sensitive:
                value:                                        ${input_x_placeholder + input_y}
                sensitive: true
            - argument_input_concat_x_y_placeholder_sensitive:
                value:                                  ${input_concat_x_y_placeholder}
                sensitive: true
            - argument_input_concat_x_y_placeholder_placeholder_sensitive:
                value:                        ${input_concat_x_y_placeholder_placeholder}
                sensitive: true
            - argument_concat_x_placeholder_y_placeholder_sensitive:
                value:                              ${input_x_placeholder + input_y_placeholder}
                sensitive: true
            - argument_input_concat_x_placeholder_y_placeholder_sensitive:
                value:                      ${input_concat_x_placeholder_y_placeholder}
                sensitive: true
            - argument_input_concat_x_placeholder_y_placeholder_placeholder_sensitive:
                value:              ${input_concat_x_placeholder_y_placeholder_placeholder}
                sensitive: true

            - argument_concat_x_folded_sensitive:
                value:                                             ${'prefix_' + input_concat_x + '_suffix'}
                sensitive: true
            - argument_input_concat_x_folded_sensitive:
                value:                                       ${input_concat_x_folded}
                sensitive: true
            - argument_input_concat_x_folded_placeholder_sensitive:
                value:                             ${input_concat_x_folded_placeholder}
                sensitive: true
            - argument_input_placeholder_concat_x_folded_sensitive:
                value:                             ${input_placeholder_concat_x_folded}
                sensitive: true
            - argument_input_placeholder_concat_x_folded_placeholder_sensitive:
                value:                   ${input_placeholder_concat_x_folded_placeholder}
                sensitive: true
            - argument_input_concat_x_folded_copy_sensitive:
                value:                                  ${input_concat_x_folded_copy}
                sensitive: true
            - argument_input_concat_x_folded_copy_placeholder_sensitive:
                value:                        ${input_concat_x_folded_copy_placeholder}
                sensitive: true
            - argument_input_placeholder_concat_x_folded_copy_sensitive:
                value:                        ${input_placeholder_concat_x_folded_copy}
                sensitive: true
            - argument_expression_characters_sensitive:
                value: >
                  ${'docker run -d -e AUTHORIZED_KEYS=${base64 -w0 ' + input_authorized_keys_path + '} -p ' + input_scp_host_port + '_sensitive:    value:22 --name test1 -v /data_sensitive:    value:'}
                sensitive: true
            - argument_input_expression_characters_sensitive:
                value:                                 ${input_expression_characters}
                sensitive: true
            - argument_input_expression_characters_placeholder_sensitive:
                value:                       ${input_expression_characters_placeholder}
                sensitive: true
            - argument_input_placeholder_expression_characters_sensitive:
                value:                       ${input_placeholder_expression_characters}
                sensitive: true
            - argument_input_placeholder_expression_characters_placeholder_sensitive:
                value:             ${input_placeholder_expression_characters_placeholder}
                sensitive: true
        navigate:
          - SUCCESS: prepare_for_print

    - prepare_for_print:
        do:
          ops.noop:
        navigate:
          - SUCCESS: SUCCESS
  results:
    - SUCCESS
