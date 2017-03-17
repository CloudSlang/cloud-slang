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
  name: sensitive_values_in_python_expressions_step_input_modifiers
  inputs:
    - int1_input: '3'
    - int1_input_placeholder:
        default: '3'
        sensitive: false

    - int2_input: '4'
    - int2_input_placeholder:
        default: '4'
        sensitive: false

    - str1_input: 'ab'
    - str1_input_placeholder:
        default: 'ab'
        sensitive: false

    - str2_input: 'bc'
    - str2_input_placeholder:
        default: 'bc'
        sensitive: false

    - float1_input: '-2.5'
    - float1_input_placeholder:
        default: '-2.5'
        sensitive: false

    - float2_input: '11.4'
    - float2_input_placeholder:
        default: '11.4'
        sensitive: false

    - binary1_input: '0b00111100'
    - binary1_input_placeholder:
        default: '0b00111100'
        sensitive: false

    - binary2_input: '0b00001101'
    - binary2_input_placeholder:
        default: '0b00001101'
        sensitive: false

  workflow:
    - get_data:
        do:
          ops.noop:
            - format1_int_arguments_sensitive:
                value:                   ${"%d and %d" %(int(int1_input),int(int2_input))}
                sensitive: true
            - format1_int_argument1_placeholder_sensitive:
                value:         ${"%d and %d" %(int(int1_input_placeholder),int(int2_input))}
                sensitive: true
            - format1_int_argument2_placeholder_sensitive:
                value:         ${"%d and %d" %(int(int1_input),int(int2_input_placeholder))}
                sensitive: true
            - format1_int_arguments_placeholder_sensitive:
                value:         ${"%d and %d" %(int(int1_input_placeholder),int(int2_input_placeholder))}
                sensitive: true

            - format1_str_arguments_sensitive:
                value:                   ${"%s and %s" %(str1_input,str2_input)}
                sensitive: true
            - format1_str_argument1_placeholder_sensitive:
                value:         ${"%s and %s" %(str1_input_placeholder,str2_input)}
                sensitive: true
            - format1_str_argument2_placeholder_sensitive:
                value:         ${"%s and %s" %(str1_input,str2_input_placeholder)}
                sensitive: true
            - format1_str_arguments_placeholder_sensitive:
                value:         ${"%s and %s" %(str1_input_placeholder,str2_input_placeholder)}
                sensitive: true

            - format2_int_arguments_sensitive:
                value:                   ${"%{0} and {1}".format(int(int1_input),int(int2_input))}
                sensitive: true
            - format2_int_argument1_placeholder_sensitive:
                value:         ${"%{0} and {1}".format(int(int1_input_placeholder),int(int2_input))}
                sensitive: true
            - format2_int_argument2_placeholder_sensitive:
                value:         ${"%{0} and {1}".format(int(int1_input),int(int2_input_placeholder))}
                sensitive: true
            - format2_int_arguments_placeholder_sensitive:
                value:         ${"%{0} and {1}".format(int(int1_input_placeholder),int(int2_input_placeholder))}
                sensitive: true

            - format2_str_arguments_sensitive:
                value:                   ${"%{0} and {1}".format(str1_input,str2_input)}
                sensitive: true
            - format2_str_argument1_placeholder_sensitive:
                value:         ${"%{0} and {1}".format(str1_input_placeholder,str2_input)}
                sensitive: true
            - format2_str_argument2_placeholder_sensitive:
                value:         ${"%{0} and {1}".format(str1_input,str2_input_placeholder)}
                sensitive: true
            - format2_str_arguments_placeholder_sensitive:
                value:         ${"%{0} and {1}".format(str1_input_placeholder,str2_input_placeholder)}
                sensitive: true

            - chr_int_argument1_sensitive:
                value:                       ${chr(int(int1_input))}
                sensitive: true
            - chr_int_argument2_sensitive:
                value:                       ${chr(int(int2_input))}
                sensitive: true
            - chr_int_argument1_placeholder_sensitive:
                value:             ${chr(int(int1_input_placeholder))}
                sensitive: true
            - chr_int_argument2_placeholder_sensitive:
                value:             ${chr(int(int2_input_placeholder))}
                sensitive: true

            - unichr_int_argument1_sensitive:
                value:                    ${unichr(int(int1_input))}
                sensitive: true
            - unichr_int_argument2_sensitive:
                value:                    ${unichr(int(int2_input))}
                sensitive: true
            - unichr_int_argument1_placeholder_sensitive:
                value:          ${unichr(int(int1_input_placeholder))}
                sensitive: true
            - unichr_int_argument2_placeholder_sensitive:
                value:          ${unichr(int(int2_input_placeholder))}
                sensitive: true

            - str_int_argument1_sensitive:
                value:                       ${str(int(int1_input))}
                sensitive: true
            - str_int_argument2_sensitive:
                value:                       ${str(int(int2_input))}
                sensitive: true
            - str_int_argument1_placeholder_sensitive:
                value:             ${str(int(int1_input_placeholder))}
                sensitive: true
            - str_int_argument2_placeholder_sensitive:
                value:             ${str(int(int2_input_placeholder))}
                sensitive: true

            - str_float_argument1_sensitive:
                value:                     ${str(float(float1_input))}
                sensitive: true
            - str_float_argument2_sensitive:
                value:                     ${str(float(float2_input))}
                sensitive: true
            - str_float_argument1_placeholder_sensitive:
                value:           ${str(float(float1_input_placeholder))}
                sensitive: true
            - str_float_argument2_placeholder_sensitive:
                value:           ${str(float(float2_input_placeholder))}
                sensitive: true

            - unicode_int_argument1_sensitive:
                value:                   ${unicode(int(int1_input))}
                sensitive: true
            - unicode_int_argument2_sensitive:
                value:                   ${unicode(int(int2_input))}
                sensitive: true
            - unicode_int_argument1_placeholder_sensitive:
                value:         ${unicode(int(int1_input_placeholder))}
                sensitive: true
            - unicode_int_argument2_placeholder_sensitive:
                value:         ${unicode(int(int2_input_placeholder))}
                sensitive: true

            - unicode_float_argument1_sensitive:
                value:                 ${unicode(float(float1_input))}
                sensitive: true
            - unicode_float_argument2_sensitive:
                value:                 ${unicode(float(float2_input))}
                sensitive: true
            - unicode_float_argument1_placeholder_sensitive:
                value:       ${unicode(float(float1_input_placeholder))}
                sensitive: true
            - unicode_float_argument2_placeholder_sensitive:
                value:       ${unicode(float(float2_input_placeholder))}
                sensitive: true

            - unicode_str_argument1_sensitive:
                value:                   ${unicode(str1_input)}
                sensitive: true
            - unicode_str_argument2_sensitive:
                value:                   ${unicode(str2_input)}
                sensitive: true
            - unicode_str_argument1_placeholder_sensitive:
                value:         ${unicode(str1_input_placeholder)}
                sensitive: true
            - unicode_str_argument2_placeholder_sensitive:
                value:         ${unicode(str2_input_placeholder)}
                sensitive: true

            - add1_str_arguments_sensitive:
                value:                      ${str1_input + str2_input}
                sensitive: true
            - add1_str_argument1_placeholder_sensitive:
                value:            ${str1_input_placeholder + str2_input}
                sensitive: true
            - add1_str_argument2_placeholder_sensitive:
                value:            ${str1_input + str2_input_placeholder}
                sensitive: true
            - add1_str_arguments_placeholder_sensitive:
                value:            ${str1_input_placeholder + str2_input_placeholder}
                sensitive: true

            - add2_str_arguments_sensitive:
                value:                      ${str1_input + 'c' + str2_input}
                sensitive: true
            - add2_str_argument1_placeholder_sensitive:
                value:            ${str1_input_placeholder + 'c' + str2_input}
                sensitive: true
            - add2_str_argument2_placeholder_sensitive:
                value:            ${str1_input + 'c' + str2_input_placeholder}
                sensitive: true
            - add2_str_arguments_placeholder_sensitive:
                value:            ${str1_input_placeholder + 'c' + str2_input_placeholder}
                sensitive: true
        navigate:
          - SUCCESS: SUCCESS

  results:
    - SUCCESS
