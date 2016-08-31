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
  name: sensitive_values_in_python_expressions_flow
  inputs:
    - int1_input: '3'
    - int1_input_sensitive:
        default: '3'
        sensitive: true

    - int2_input: '4'
    - int2_input_sensitive:
        default: '4'
        sensitive: true

    - str1_input: 'ab'
    - str1_input_sensitive:
        default: 'ab'
        sensitive: true

    - str2_input: 'bc'
    - str2_input_sensitive:
        default: 'bc'
        sensitive: true

    - float1_input: '-2.5'
    - float1_input_sensitive:
        default: '-2.5'
        sensitive: true

    - float2_input: '11.4'
    - float2_input_sensitive:
        default: '11.4'
        sensitive: true

    - binary1_input: '0b00111100'
    - binary1_input_sensitive:
        default: '0b00111100'
        sensitive: true

    - binary2_input: '0b00001101'
    - binary2_input_sensitive:
        default: '0b00001101'
        sensitive: true
# uncomment when types will be supported
#    - list1_input: ${[1, 2, 3]}
#    - list1_input_sensitive:
#        default: ${[1, 2, 3]}
#        sensitive: true
#
#    - list2_input: ${[5, 10, 15]}
#    - list2_input_sensitive:
#        default: ${[5, 10, 15]}
#        sensitive: true

  workflow:
    - get_data:
        do:
          ops.noop:
#            - cmp_int_arguments:                       ${int(int1_input) == int(int2_input)}
#            - cmp_int_argument1_sensitive:             ${int(int1_input_sensitive) == int(int2_input)}
#            - cmp_int_argument2_sensitive:             ${int(int1_input) == int(int2_input_sensitive)}
#            - cmp_int_arguments_sensitive:             ${int(int1_input_sensitive) == int(int2_input_sensitive)}
#
#            - cmp_str_arguments:                       ${str1_input == str2_input}
#            - cmp_str_argument1_sensitive:             ${str1_input_sensitive == str2_input}
#            - cmp_str_argument2_sensitive:             ${str1_input == str2_input_sensitive}
#            - cmp_str_arguments_sensitive:             ${str1_input_sensitive == str2_input_sensitive}
#
#            - eq_int_arguments:                        ${int(int1_input) == int(int2_input)}
#            - eq_int_argument1_sensitive:              ${int(int1_input_sensitive) == int(int2_input)}
#            - eq_int_argument2_sensitive:              ${int(int1_input) == int(int2_input_sensitive)}
#            - eq_int_arguments_sensitive:              ${int(int1_input_sensitive) == int(int2_input_sensitive)}
#
#            - eq_str_arguments:                        ${str1_input == str2_input}
#            - eq_str_argument1_sensitive:              ${str1_input_sensitive == str2_input}
#            - eq_str_argument2_sensitive:              ${str1_input == str2_input_sensitive}
#            - eq_str_arguments_sensitive:              ${str1_input_sensitive == str2_input_sensitive}
#
#            - ne1_int_arguments:                       ${int(int1_input) != int(int2_input)}
#            - ne1_int_argument1_sensitive:             ${int(int1_input_sensitive) != int(int2_input)}
#            - ne1_int_argument2_sensitive:             ${int(int1_input) != int(int2_input_sensitive)}
#            - ne1_int_arguments_sensitive:             ${int(int1_input_sensitive) != int(int2_input_sensitive)}
#
#            - ne1_str_arguments:                       ${str1_input != str2_input}
#            - ne1_str_argument1_sensitive:             ${str1_input_sensitive != str2_input}
#            - ne1_str_argument2_sensitive:             ${str1_input != str2_input_sensitive}
#            - ne1_str_arguments_sensitive:             ${str1_input_sensitive != str2_input_sensitive}
#
#            - ne2_int_arguments:                       ${int(int1_input) <> int(int2_input)}
#            - ne2_int_argument1_sensitive:             ${int(int1_input_sensitive) <> int(int2_input)}
#            - ne2_int_argument2_sensitive:             ${int(int1_input) <> int(int2_input_sensitive)}
#            - ne2_int_arguments_sensitive:             ${int(int1_input_sensitive) <> int(int2_input_sensitive)}
#
#            - ne2_str_arguments:                       ${str1_input <> str2_input}
#            - ne2_str_argument1_sensitive:             ${str1_input_sensitive <> str2_input}
#            - ne2_str_argument2_sensitive:             ${str1_input <> str2_input_sensitive}
#            - ne2_str_arguments_sensitive:             ${str1_input_sensitive <> str2_input_sensitive}
#
#            - le_int_arguments:                        ${int(int1_input) <= int(int2_input)}
#            - le_int_argument1_sensitive:              ${int(int1_input_sensitive) <= int(int2_input)}
#            - le_int_argument2_sensitive:              ${int(int1_input) <= int(int2_input_sensitive)}
#            - le_int_arguments_sensitive:              ${int(int1_input_sensitive) <= int(int2_input_sensitive)}
#
#            - le_str_arguments:                        ${str1_input <= str2_input}
#            - le_str_argument1_sensitive:              ${str1_input_sensitive <= str2_input}
#            - le_str_argument2_sensitive:              ${str1_input <= str2_input_sensitive}
#            - le_str_arguments_sensitive:              ${str1_input_sensitive <= str2_input_sensitive}
#
#            - lt_int_arguments:                        ${int(int1_input) < int(int2_input)}
#            - lt_int_argument1_sensitive:              ${int(int1_input_sensitive) < int(int2_input)}
#            - lt_int_argument2_sensitive:              ${int(int1_input) < int(int2_input_sensitive)}
#            - lt_int_arguments_sensitive:              ${int(int1_input_sensitive) < int(int2_input_sensitive)}
#
#            - lt_str_arguments:                        ${str1_input < str2_input}
#            - lt_str_argument1_sensitive:              ${str1_input_sensitive < str2_input}
#            - lt_str_argument2_sensitive:              ${str1_input < str2_input_sensitive}
#            - lt_str_arguments_sensitive:              ${str1_input_sensitive < str2_input_sensitive}
#
#            - ge_int_arguments:                        ${int(int1_input) >= int(int2_input)}
#            - ge_int_argument1_sensitive:              ${int(int1_input_sensitive) >= int(int2_input)}
#            - ge_int_argument2_sensitive:              ${int(int1_input) >= int(int2_input_sensitive)}
#            - ge_int_arguments_sensitive:              ${int(int1_input_sensitive) >= int(int2_input_sensitive)}
#
#            - ge_str_arguments:                        ${str1_input >= str2_input}
#            - ge_str_argument1_sensitive:              ${str1_input_sensitive >= str2_input}
#            - ge_str_argument2_sensitive:              ${str1_input >= str2_input_sensitive}
#            - ge_str_arguments_sensitive:              ${str1_input_sensitive >= str2_input_sensitive}
#
#            - gt_int_arguments:                        ${int(int1_input) > int(int2_input)}
#            - gt_int_argument1_sensitive:              ${int(int1_input_sensitive) > int(int2_input)}
#            - gt_int_argument2_sensitive:              ${int(int1_input) > int(int2_input_sensitive)}
#            - gt_int_arguments_sensitive:              ${int(int1_input_sensitive) > int(int2_input_sensitive)}
#
#            - gt_str_arguments:                        ${str1_input > str2_input}
#            - gt_str_argument1_sensitive:              ${str1_input_sensitive > str2_input}
#            - gt_str_argument2_sensitive:              ${str1_input > str2_input_sensitive}
#            - gt_str_arguments_sensitive:              ${str1_input_sensitive > str2_input_sensitive}
#
#            - is_int_arguments:                        ${int(int1_input) is int(int2_input)}
#            - is_int_argument1_sensitive:              ${int(int1_input_sensitive) is int(int2_input)}
#            - is_int_argument2_sensitive:              ${int(int1_input) is int(int2_input_sensitive)}
#            - is_int_arguments_sensitive:              ${int(int1_input_sensitive) is int(int2_input_sensitive)}
#
#            - is_str_arguments:                        ${str1_input is str2_input}
#            - is_str_argument1_sensitive:              ${str1_input_sensitive is str2_input}
#            - is_str_argument2_sensitive:              ${str1_input is str2_input_sensitive}
#            - is_str_arguments_sensitive:              ${str1_input_sensitive is str2_input_sensitive}
#
#            - is_not_int_arguments:                    ${int(int1_input) is not int(int2_input)}
#            - is_not_int_argument1_sensitive:          ${int(int1_input_sensitive) is not int(int2_input)}
#            - is_not_int_argument2_sensitive:          ${int(int1_input) is not int(int2_input_sensitive)}
#            - is_not_int_arguments_sensitive:          ${int(int1_input_sensitive) is not int(int2_input_sensitive)}
#
#            - is_not_str_arguments:                    ${str1_input is not str2_input}
#            - is_not_str_argument1_sensitive:          ${str1_input_sensitive is not str2_input}
#            - is_not_str_argument2_sensitive:          ${str1_input is not str2_input_sensitive}
#            - is_not_str_arguments_sensitive:          ${str1_input_sensitive is not str2_input_sensitive}
#
#            - in_str_argument1:                        ${str1_input in 'abcd'}
#            - in_str_argument2:                        ${str2_input in 'abcd'}
#            - in_str_argument1_sensitive:              ${str1_input_sensitive in 'abcd'}
#            - in_str_argument2_sensitive:              ${str2_input_sensitive in 'abcd'}
#
#            - not_in_str_argument1:                    ${str1_input not in 'abcd'}
#            - not_in_str_argument2:                    ${str2_input not in 'abcd'}
#            - not_in_str_argument1_sensitive:          ${str1_input_sensitive not in 'abcd'}
#            - not_in_str_argument2_sensitive:          ${str2_input_sensitive not in 'abcd'}
#
#            - len_str_argument1:                       ${len(str1_input)}
#            - len_str_argument2:                       ${len(str2_input)}
#            - len_str_argument1_sensitive:             ${len(str1_input_sensitive)}
#            - len_str_argument2_sensitive:             ${len(str2_input_sensitive)}

            - format1_int_arguments:                   ${"%d and %d" %(int(int1_input),int(int2_input))}
            - format1_int_argument1_sensitive:         ${"%d and %d" %(int(int1_input_sensitive),int(int2_input))}
            - format1_int_argument2_sensitive:         ${"%d and %d" %(int(int1_input),int(int2_input_sensitive))}
            - format1_int_arguments_sensitive:         ${"%d and %d" %(int(int1_input_sensitive),int(int2_input_sensitive))}

            - format1_str_arguments:                   ${"%s and %s" %(str1_input,str2_input)}
            - format1_str_argument1_sensitive:         ${"%s and %s" %(str1_input_sensitive,str2_input)}
            - format1_str_argument2_sensitive:         ${"%s and %s" %(str1_input,str2_input_sensitive)}
            - format1_str_arguments_sensitive:         ${"%s and %s" %(str1_input_sensitive,str2_input_sensitive)}

            - format2_int_arguments:                   ${"%{0} and {1}".format(int(int1_input),int(int2_input))}
            - format2_int_argument1_sensitive:         ${"%{0} and {1}".format(int(int1_input_sensitive),int(int2_input))}
            - format2_int_argument2_sensitive:         ${"%{0} and {1}".format(int(int1_input),int(int2_input_sensitive))}
            - format2_int_arguments_sensitive:         ${"%{0} and {1}".format(int(int1_input_sensitive),int(int2_input_sensitive))}

            - format2_str_arguments:                   ${"%{0} and {1}".format(str1_input,str2_input)}
            - format2_str_argument1_sensitive:         ${"%{0} and {1}".format(str1_input_sensitive,str2_input)}
            - format2_str_argument2_sensitive:         ${"%{0} and {1}".format(str1_input,str2_input_sensitive)}
            - format2_str_arguments_sensitive:         ${"%{0} and {1}".format(str1_input_sensitive,str2_input_sensitive)}

#            - and_binary_arguments:                    ${int(binary1_input, 2) & int(binary2_input, 2)}
#            - and_binary_argument1_sensitive:          ${int(binary1_input_sensitive, 2) & int(binary2_input, 2)}
#            - and_binary_argument2_sensitive:          ${int(binary1_input, 2) & int(binary2_input_sensitive, 2)}
#            - and_binary_arguments_sensitive:          ${int(binary1_input_sensitive, 2) & int(binary2_input_sensitive, 2)}
#
#            - or_binary_arguments:                     ${int(binary1_input, 2) | int(binary2_input, 2)}
#            - or_binary_argument1_sensitive:           ${int(binary1_input_sensitive, 2) | int(binary2_input, 2)}
#            - or_binary_argument2_sensitive:           ${int(binary1_input, 2) | int(binary2_input_sensitive, 2)}
#            - or_binary_arguments_sensitive:           ${int(binary1_input_sensitive, 2) | int(binary2_input_sensitive, 2)}
#
#            - xor_binary_arguments:                    ${int(binary1_input, 2) ^ int(binary2_input, 2)}
#            - xor_binary_argument1_sensitive:          ${int(binary1_input_sensitive, 2) ^ int(binary2_input, 2)}
#            - xor_binary_argument2_sensitive:          ${int(binary1_input, 2) ^ int(binary2_input_sensitive, 2)}
#            - xor_binary_arguments_sensitive:          ${int(binary1_input_sensitive, 2) ^ int(binary2_input_sensitive, 2)}
#
#            - not_binary_argument1:                    ${~int(binary1_input, 2)}
#            - not_binary_argument2:                    ${~int(binary2_input, 2)}
#            - not_binary_argument1_sensitive:          ${~int(binary1_input_sensitive, 2)}
#            - not_binary_argument2_sensitive:          ${~int(binary2_input_sensitive, 2)}
#
#            - lshift_binary_argument1:                 ${int(binary1_input, 2) << 4}
#            - lshift_binary_argument2:                 ${int(binary2_input, 2) << 4}
#            - lshift_binary_argument1_sensitive:       ${int(binary1_input_sensitive, 2) << 4}
#            - lshift_binary_argument2_sensitive:       ${int(binary2_input_sensitive, 2) << 4}
#
#            - rshift_binary_argument1:                 ${int(binary1_input, 2) >> 16}
#            - rshift_binary_argument2:                 ${int(binary2_input, 2) >> 16}
#            - rshift_binary_argument1_sensitive:       ${int(binary1_input_sensitive, 2) >> 16}
#            - rshift_binary_argument2_sensitive:       ${int(binary2_input_sensitive, 2) >> 16}
#
#            - hex_binary_argument1:                    ${hex(int(binary1_input, 2))}
#            - hex_binary_argument2:                    ${hex(int(binary2_input, 2))}
#            - hex_binary_argument1_sensitive:          ${hex(int(binary1_input_sensitive, 2))}
#            - hex_binary_argument2_sensitive:          ${hex(int(binary2_input_sensitive, 2))}
#
#            - oct_binary_argument1:                    ${oct(int(binary1_input, 2))}
#            - oct_binary_argument2:                    ${oct(int(binary2_input, 2))}
#            - oct_binary_argument1_sensitive:          ${oct(int(binary1_input_sensitive, 2))}
#            - oct_binary_argument2_sensitive:          ${oct(int(binary2_input_sensitive, 2))}
#
#            - int_float_argument1:                     ${int(float(float1_input))}
#            - int_float_argument2:                     ${int(float(float2_input))}
#            - int_float_argument1_sensitive:           ${int(float(float1_input_sensitive))}
#            - int_float_argument2_sensitive:           ${int(float(float2_input_sensitive))}
#
#            - long_float_argument1:                    ${long(float(float1_input))}
#            - long_float_argument2:                    ${long(float(float2_input))}
#            - long_float_argument1_sensitive:          ${long(float(float1_input_sensitive))}
#            - long_float_argument2_sensitive:          ${long(float(float2_input_sensitive))}
#
#            - float_int_argument1:                     ${float(int(int1_input))}
#            - float_int_argument2:                     ${float(int(int2_input))}
#            - float_int_argument1_sensitive:           ${float(int(int1_input_sensitive))}
#            - float_int_argument2_sensitive:           ${float(int(int2_input_sensitive))}

            - chr_int_argument1:                       ${chr(int(int1_input))}
            - chr_int_argument2:                       ${chr(int(int2_input))}
            - chr_int_argument1_sensitive:             ${chr(int(int1_input_sensitive))}
            - chr_int_argument2_sensitive:             ${chr(int(int2_input_sensitive))}

            - unichr_int_argument1:                    ${unichr(int(int1_input))}
            - unichr_int_argument2:                    ${unichr(int(int2_input))}
            - unichr_int_argument1_sensitive:          ${unichr(int(int1_input_sensitive))}
            - unichr_int_argument2_sensitive:          ${unichr(int(int2_input_sensitive))}

#            - bool_int_argument1:                      ${bool(int(int1_input))}
#            - bool_int_argument2:                      ${bool(int(int2_input))}
#            - bool_int_argument1_sensitive:            ${bool(int(int1_input_sensitive))}
#            - bool_int_argument2_sensitive:            ${bool(int(int2_input_sensitive))}

            - str_int_argument1:                       ${str(int(int1_input))}
            - str_int_argument2:                       ${str(int(int2_input))}
            - str_int_argument1_sensitive:             ${str(int(int1_input_sensitive))}
            - str_int_argument2_sensitive:             ${str(int(int2_input_sensitive))}

            - str_float_argument1:                     ${str(float(float1_input))}
            - str_float_argument2:                     ${str(float(float2_input))}
            - str_float_argument1_sensitive:           ${str(float(float1_input_sensitive))}
            - str_float_argument2_sensitive:           ${str(float(float2_input_sensitive))}

            - unicode_int_argument1:                   ${unicode(int(int1_input))}
            - unicode_int_argument2:                   ${unicode(int(int2_input))}
            - unicode_int_argument1_sensitive:         ${unicode(int(int1_input_sensitive))}
            - unicode_int_argument2_sensitive:         ${unicode(int(int2_input_sensitive))}

            - unicode_float_argument1:                 ${unicode(float(float1_input))}
            - unicode_float_argument2:                 ${unicode(float(float2_input))}
            - unicode_float_argument1_sensitive:       ${unicode(float(float1_input_sensitive))}
            - unicode_float_argument2_sensitive:       ${unicode(float(float2_input_sensitive))}

            - unicode_str_argument1:                   ${unicode(str1_input)}
            - unicode_str_argument2:                   ${unicode(str2_input)}
            - unicode_str_argument1_sensitive:         ${unicode(str1_input_sensitive)}
            - unicode_str_argument2_sensitive:         ${unicode(str2_input_sensitive)}

#            - trunc_float_argument1:                   ${int(float(float1_input))}
#            - trunc_float_argument2:                   ${int(float(float2_input))}
#            - trunc_float_argument1_sensitive:         ${int(float(float1_input_sensitive))}
#            - trunc_float_argument2_sensitive:         ${int(float(float2_input_sensitive))}
#
#            - range_int_argument1:                     ${range(int(int1_input))}
#            - range_int_argument2:                     ${range(int(int2_input))}
#            - range_int_argument1_sensitive:           ${range(int(int1_input_sensitive))}
#            - range_int_argument2_sensitive:           ${range(int(int2_input_sensitive))}
#
#            - pos_int_argument1:                       ${+int(int1_input)}
#            - pos_int_argument2:                       ${+int(int2_input)}
#            - pos_int_argument1_sensitive:             ${+int(int1_input_sensitive)}
#            - pos_int_argument2_sensitive:             ${+int(int2_input_sensitive)}
#
#            - pos_float_argument1:                     ${+float(float1_input)}
#            - pos_float_argument2:                     ${+float(float2_input)}
#            - pos_float_argument1_sensitive:           ${+float(float1_input_sensitive)}
#            - pos_float_argument2_sensitive:           ${+float(float2_input_sensitive)}
#
#            - neg_int_argument1:                       ${-int(int1_input)}
#            - neg_int_argument2:                       ${-int(int2_input)}
#            - neg_int_argument1_sensitive:             ${-int(int1_input_sensitive)}
#            - neg_int_argument2_sensitive:             ${-int(int2_input_sensitive)}
#
#            - neg_float_argument1:                     ${-float(float1_input)}
#            - neg_float_argument2:                     ${-float(float2_input)}
#            - neg_float_argument1_sensitive:           ${-float(float1_input_sensitive)}
#            - neg_float_argument2_sensitive:           ${-float(float2_input_sensitive)}
#
#            - abs_int_argument1:                       ${abs(int(int1_input))}
#            - abs_int_argument2:                       ${abs(int(int2_input))}
#            - abs_int_argument1_sensitive:             ${abs(int(int1_input_sensitive))}
#            - abs_int_argument2_sensitive:             ${abs(int(int2_input_sensitive))}
#
#            - abs_float_argument1:                     ${abs(float(float1_input))}
#            - abs_float_argument2:                     ${abs(float(float2_input))}
#            - abs_float_argument1_sensitive:           ${abs(float(float1_input_sensitive))}
#            - abs_float_argument2_sensitive:           ${abs(float(float2_input_sensitive))}
#
#            - round_int_argument1:                     ${round(int(int1_input))}
#            - round_int_argument2:                     ${round(int(int2_input))}
#            - round_int_argument1_sensitive:           ${round(int(int1_input_sensitive))}
#            - round_int_argument2_sensitive:           ${round(int(int2_input_sensitive))}
#
#            - round_float_argument1:                   ${round(float(float1_input))}
#            - round_float_argument2:                   ${round(float(float2_input))}
#            - round_float_argument1_sensitive:         ${round(float(float1_input_sensitive))}
#            - round_float_argument2_sensitive:         ${round(float(float2_input_sensitive))}
#
#            - pow1_int_argument1:                      ${pow(int(int1_input), 2)}
#            - pow1_int_argument2:                      ${pow(int(int2_input), 2)}
#            - pow1_int_argument1_sensitive:            ${pow(int(int1_input_sensitive), 2)}
#            - pow1_int_argument2_sensitive:            ${pow(int(int2_input_sensitive), 2)}
#
#            - pow1_float_argument1:                    ${pow(float(float1_input), 2)}
#            - pow1_float_argument2:                    ${pow(float(float2_input), 2)}
#            - pow1_float_argument1_sensitive:          ${pow(float(float1_input_sensitive), 2)}
#            - pow1_float_argument2_sensitive:          ${pow(float(float2_input_sensitive), 2)}
#
#            - pow2_int_argument1:                      ${int(int1_input) ** 2}
#            - pow2_int_argument2:                      ${int(int2_input) ** 2}
#            - pow2_int_argument1_sensitive:            ${int(int1_input_sensitive) ** 2}
#            - pow2_int_argument2_sensitive:            ${int(int2_input_sensitive) ** 2}
#
#            - pow2_float_argument1:                    ${float(float1_input) ** 2}
#            - pow2_float_argument2:                    ${float(float2_input) ** 2}
#            - pow2_float_argument1_sensitive:          ${float(float1_input_sensitive) ** 2}
#            - pow2_float_argument2_sensitive:          ${float(float2_input_sensitive) ** 2}
#
#            - max_int_arguments:                       ${max(int(int1_input), int(int2_input))}
#            - max_int_argument1_sensitive:             ${max(int(int1_input_sensitive), int(int2_input))}
#            - max_int_argument2_sensitive:             ${max(int(int1_input), int(int2_input_sensitive))}
#            - max_int_arguments_sensitive:             ${max(int(int1_input_sensitive), int(int2_input_sensitive))}
#
#            - max_float_arguments:                     ${max(float(float1_input), float(float2_input))}
#            - max_float_argument1_sensitive:           ${max(float(float1_input_sensitive), float(float2_input))}
#            - max_float_argument2_sensitive:           ${max(float(float1_input), float(float2_input_sensitive))}
#            - max_float_arguments_sensitive:           ${max(float(float1_input_sensitive), float(float2_input_sensitive))}
#
#            - max_str_arguments:                       ${max(str1_input, str2_input)}
#            - max_str_argument1_sensitive:             ${max(str1_input_sensitive, str2_input)}
#            - max_str_argument2_sensitive:             ${max(str1_input, str2_input_sensitive)}
#            - max_str_arguments_sensitive:             ${max(str1_input_sensitive, str2_input_sensitive)}
#
#            - min_int_arguments:                       ${min(int(int1_input), int(int2_input))}
#            - min_int_argument1_sensitive:             ${min(int(int1_input_sensitive), int(int2_input))}
#            - min_int_argument2_sensitive:             ${min(int(int1_input), int(int2_input_sensitive))}
#            - min_int_arguments_sensitive:             ${min(int(int1_input_sensitive), int(int2_input_sensitive))}
#
#            - min_float_arguments:                     ${min(float(float1_input), float(float2_input))}
#            - min_float_argument1_sensitive:           ${min(float(float1_input_sensitive), float(float2_input))}
#            - min_float_argument2_sensitive:           ${min(float(float1_input), float(float2_input_sensitive))}
#            - min_float_arguments_sensitive:           ${min(float(float1_input_sensitive), float(float2_input_sensitive))}
#
#            - min_str_arguments:                       ${min(str1_input, str2_input)}
#            - min_str_argument1_sensitive:             ${min(str1_input_sensitive, str2_input)}
#            - min_str_argument2_sensitive:             ${min(str1_input, str2_input_sensitive)}
#            - min_str_arguments_sensitive:             ${min(str1_input_sensitive, str2_input_sensitive)}
#
#            - add1_int_arguments:                      ${int(int1_input) + int(int2_input)}
#            - add1_int_argument1_sensitive:            ${int(int1_input_sensitive) + int(int2_input)}
#            - add1_int_argument2_sensitive:            ${int(int1_input) + int(int2_input_sensitive)}
#            - add1_int_arguments_sensitive:            ${int(int1_input_sensitive) + int(int2_input_sensitive)}

            - add1_str_arguments:                      ${str1_input + str2_input}
            - add1_str_argument1_sensitive:            ${str1_input_sensitive + str2_input}
            - add1_str_argument2_sensitive:            ${str1_input + str2_input_sensitive}
            - add1_str_arguments_sensitive:            ${str1_input_sensitive + str2_input_sensitive}

#            - add1_float_arguments:                    ${float(float1_input) + float(float2_input)}
#            - add1_float_argument1_sensitive:          ${float(float1_input_sensitive) + float(float2_input)}
#            - add1_float_argument2_sensitive:          ${float(float1_input) + float(float2_input_sensitive)}
#            - add1_float_arguments_sensitive:          ${float(float1_input_sensitive) + float(float2_input_sensitive)}

#            - add2_int_arguments:                      ${int(int1_input) + 5 + int(int2_input)}
#            - add2_int_argument1_sensitive:            ${int(int1_input_sensitive) + 5 + int(int2_input)}
#            - add2_int_argument2_sensitive:            ${int(int1_input) + 5 + int(int2_input_sensitive)}
#            - add2_int_arguments_sensitive:            ${int(int1_input_sensitive) + 5 + int(int2_input_sensitive)}

            - add2_str_arguments:                      ${str1_input + 'c' + str2_input}
            - add2_str_argument1_sensitive:            ${str1_input_sensitive + 'c' + str2_input}
            - add2_str_argument2_sensitive:            ${str1_input + 'c' + str2_input_sensitive}
            - add2_str_arguments_sensitive:            ${str1_input_sensitive + 'c' + str2_input_sensitive}

#            - add2_float_arguments:                    ${float(float1_input) + 7.0 + float(float2_input)}
#            - add2_float_argument1_sensitive:          ${float(float1_input_sensitive) + 7.0 + float(float2_input)}
#            - add2_float_argument2_sensitive:          ${float(float1_input) + 7.0 + float(float2_input_sensitive)}
#            - add2_float_arguments_sensitive:          ${float(float1_input_sensitive) + 7.0 + float(float2_input_sensitive)}
#
#            - sub_int_arguments:                       ${int(int1_input) - int(int2_input)}
#            - sub_int_argument1_sensitive:             ${int(int1_input_sensitive) - int(int2_input)}
#            - sub_int_argument2_sensitive:             ${int(int1_input) - int(int2_input_sensitive)}
#            - sub_int_arguments_sensitive:             ${int(int1_input_sensitive) - int(int2_input_sensitive)}
#
#            - sub_float_arguments:                     ${float(float1_input) - float(float2_input)}
#            - sub_float_argument1_sensitive:           ${float(float1_input_sensitive) - float(float2_input)}
#            - sub_float_argument2_sensitive:           ${float(float1_input) - float(float2_input_sensitive)}
#            - sub_float_arguments_sensitive:           ${float(float1_input_sensitive) - float(float2_input_sensitive)}
#
#            - mul_int_arguments:                       ${int(int1_input) * int(int2_input)}
#            - mul_int_argument1_sensitive:             ${int(int1_input_sensitive) * int(int2_input)}
#            - mul_int_argument2_sensitive:             ${int(int1_input) * int(int2_input_sensitive)}
#            - mul_int_arguments_sensitive:             ${int(int1_input_sensitive) * int(int2_input_sensitive)}
#
#            - mul_float_arguments:                     ${float(float1_input) * float(float2_input)}
#            - mul_float_argument1_sensitive:           ${float(float1_input_sensitive) * float(float2_input)}
#            - mul_float_argument2_sensitive:           ${float(float1_input) * float(float2_input_sensitive)}
#            - mul_float_arguments_sensitive:           ${float(float1_input_sensitive) * float(float2_input_sensitive)}
#
#            - div_int_arguments:                       ${int(int1_input) / int(int2_input)}
#            - div_int_argument1_sensitive:             ${int(int1_input_sensitive) / int(int2_input)}
#            - div_int_argument2_sensitive:             ${int(int1_input) / int(int2_input_sensitive)}
#            - div_int_arguments_sensitive:             ${int(int1_input_sensitive) / int(int2_input_sensitive)}
#
#            - div_float_arguments:                     ${float(float1_input) / float(float2_input)}
#            - div_float_argument1_sensitive:           ${float(float1_input_sensitive) / float(float2_input)}
#            - div_float_argument2_sensitive:           ${float(float1_input) / float(float2_input_sensitive)}
#            - div_float_arguments_sensitive:           ${float(float1_input_sensitive) / float(float2_input_sensitive)}
#
#            - truediv_int_arguments:                   ${int(int1_input) / int(int2_input)}
#            - truediv_int_argument1_sensitive:         ${int(int1_input_sensitive) / int(int2_input)}
#            - truediv_int_argument2_sensitive:         ${int(int1_input) / int(int2_input_sensitive)}
#            - truediv_int_arguments_sensitive:         ${int(int1_input_sensitive) / int(int2_input_sensitive)}
#
#            - truediv_float_arguments:                 ${float(float1_input) / float(float2_input)}
#            - truediv_float_argument1_sensitive:       ${float(float1_input_sensitive) / float(float2_input)}
#            - truediv_float_argument2_sensitive:       ${float(float1_input) / float(float2_input_sensitive)}
#            - truediv_float_arguments_sensitive:       ${float(float1_input_sensitive) / float(float2_input_sensitive)}
#
#            - floordiv_int_arguments:                  ${int(int1_input) // int(int2_input)}
#            - floordiv_int_argument1_sensitive:        ${int(int1_input_sensitive) // int(int2_input)}
#            - floordiv_int_argument2_sensitive:        ${int(int1_input) // int(int2_input_sensitive)}
#            - floordiv_int_arguments_sensitive:        ${int(int1_input_sensitive) // int(int2_input_sensitive)}
#
#            - floordiv_float_arguments:                ${float(float1_input) // float(float2_input)}
#            - floordiv_float_argument1_sensitive:      ${float(float1_input_sensitive) // float(float2_input)}
#            - floordiv_float_argument2_sensitive:      ${float(float1_input) // float(float2_input_sensitive)}
#            - floordiv_float_arguments_sensitive:      ${float(float1_input_sensitive) // float(float2_input_sensitive)}
#
#            - mod_int_arguments:                       ${int(int1_input) % int(int2_input)}
#            - mod_int_argument1_sensitive:             ${int(int1_input_sensitive) % int(int2_input)}
#            - mod_int_argument2_sensitive:             ${int(int1_input) % int(int2_input_sensitive)}
#            - mod_int_arguments_sensitive:             ${int(int1_input_sensitive) % int(int2_input_sensitive)}
#
#            - mod_float_arguments:                     ${float(float1_input) % float(float2_input)}
#            - mod_float_argument1_sensitive:           ${float(float1_input_sensitive) % float(float2_input)}
#            - mod_float_argument2_sensitive:           ${float(float1_input) % float(float2_input_sensitive)}
#            - mod_float_arguments_sensitive:           ${float(float1_input_sensitive) % float(float2_input_sensitive)}
#
#            - divmod_int_argument1:                    ${divmod(int(int1_input), 2)}
#            - divmod_int_argument2:                    ${divmod(int(int2_input), 2)}
#            - divmod_int_argument1_sensitive:          ${divmod(int(int1_input_sensitive), 2)}
#            - divmod_int_argument2_sensitive:          ${divmod(int(int2_input_sensitive), 2)}
#
#            - divmod_float_argument1:                  ${divmod(float(float1_input), 2)}
#            - divmod_float_argument2:                  ${divmod(float(float2_input), 2)}
#            - divmod_float_argument1_sensitive:        ${divmod(float(float1_input_sensitive), 2)}
#            - divmod_float_argument2_sensitive:        ${divmod(float(float2_input_sensitive), 2)}
#
#            - repr_int_argument1:                      ${repr(int(int1_input))}
#            - repr_int_argument2:                      ${repr(int(int2_input))}
#            - repr_int_argument1_sensitive:            ${repr(int(int1_input_sensitive))}
#            - repr_int_argument2_sensitive:            ${repr(int(int2_input_sensitive))}
#
#            - repr_float_argument1:                    ${repr(float(float1_input))}
#            - repr_float_argument2:                    ${repr(float(float2_input))}
#            - repr_float_argument1_sensitive:          ${repr(float(float1_input_sensitive))}
#            - repr_float_argument2_sensitive:          ${repr(float(float2_input_sensitive))}
#
#            - repr_str_argument1:                      ${repr(str1_input)}
#            - repr_str_argument2:                      ${repr(str2_input)}
#            - repr_str_argument1_sensitive:            ${repr(str1_input_sensitive)}
#            - repr_str_argument2_sensitive:            ${repr(str2_input_sensitive)}

#            - index_list_argument1:                    ${list1_input.index(2)}
#            - index_list_argument2:                    ${list2_input.index(15)}
#            - index_list_argument1_sensitive:          ${list1_input_sensitive.index(2)}
#            - index_list_argument2_sensitive:          ${list2_input_sensitive.index(15)}
#
#            - sorted_list_argument1:                   ${sorted(list1_input)}
#            - sorted_list_argument2:                   ${sorted(list2_input)}
#            - sorted_list_argument1_sensitive:         ${sorted(list1_input_sensitive)}
#            - sorted_list_argument2_sensitive:         ${sorted(list2_input_sensitive)}
#
#            - sum_list_argument1:                      ${sum(list1_input, 0)}
#            - sum_list_argument2:                      ${sum(list2_input, 0)}
#            - sum_list_argument1_sensitive:            ${sum(list1_input_sensitive, 0)}
#            - sum_list_argument2_sensitive:            ${sum(list2_input_sensitive, 0)}
#
#            - tuple_list_argument1:                    ${tuple(list1_input)}
#            - tuple_list_argument2:                    ${tuple(list2_input)}
#            - tuple_list_argument1_sensitive:          ${tuple(list1_input_sensitive)}
#            - tuple_list_argument2_sensitive:          ${tuple(list2_input_sensitive)}

#            - type_int_argument1:                      ${type(int(int1_input))}
#            - type_int_argument2:                      ${type(int(int2_input))}
#            - type_int_argument1_sensitive:            ${type(int(int1_input_sensitive))}
#            - type_int_argument2_sensitive:            ${type(int(int2_input_sensitive))}
#
#            - type_float_argument1:                    ${type(float(float1_input))}
#            - type_float_argument2:                    ${type(float(float2_input))}
#            - type_float_argument1_sensitive:          ${type(float(float1_input_sensitive))}
#            - type_float_argument2_sensitive:          ${type(float(float2_input_sensitive))}
#
#            - type_str_argument1:                      ${type(str1_input)}
#            - type_str_argument2:                      ${type(str2_input)}
#            - type_str_argument1_sensitive:            ${type(str1_input_sensitive)}
#            - type_str_argument2_sensitive:            ${type(str2_input_sensitive)}
#
#            - type_list_argument1:                     ${type(list1_input)}
#            - type_list_argument2:                     ${type(list2_input)}
#            - type_list_argument1_sensitive:           ${type(list1_input_sensitive)}
#            - type_list_argument2_sensitive:           ${type(list2_input_sensitive)}
#
#        publish:
#          - cmp_int_arguments
#          - cmp_int_argument1_sensitive
#          - cmp_int_argument2_sensitive
#          - cmp_int_arguments_sensitive
#          - cmp_str_arguments
#          - cmp_str_argument1_sensitive
#          - cmp_str_argument2_sensitive
#          - cmp_str_arguments_sensitive
#          - eq_int_arguments
#          - eq_int_argument1_sensitive
#          - eq_int_argument2_sensitive
#          - eq_int_arguments_sensitive
#          - eq_str_arguments
#          - eq_str_argument1_sensitive
#          - eq_str_argument2_sensitive
#          - eq_str_arguments_sensitive
#          - ne1_int_arguments
#          - ne1_int_argument1_sensitive
#          - ne1_int_argument2_sensitive
#          - ne1_int_arguments_sensitive
#          - ne1_str_arguments
#          - ne1_str_argument1_sensitive
#          - ne1_str_argument2_sensitive
#          - ne1_str_arguments_sensitive
#          - ne2_int_arguments
#          - ne2_int_argument1_sensitive
#          - ne2_int_argument2_sensitive
#          - ne2_int_arguments_sensitive
#          - ne2_str_arguments
#          - ne2_str_argument1_sensitive
#          - ne2_str_argument2_sensitive
#          - ne2_str_arguments_sensitive
#          - le_int_arguments
#          - le_int_argument1_sensitive
#          - le_int_argument2_sensitive
#          - le_int_arguments_sensitive
#          - le_str_arguments
#          - le_str_argument1_sensitive
#          - le_str_argument2_sensitive
#          - le_str_arguments_sensitive
#          - lt_int_arguments
#          - lt_int_argument1_sensitive
#          - lt_int_argument2_sensitive
#          - lt_int_arguments_sensitive
#          - lt_str_arguments
#          - lt_str_argument1_sensitive
#          - lt_str_argument2_sensitive
#          - lt_str_arguments_sensitive
#          - ge_int_arguments
#          - ge_int_argument1_sensitive
#          - ge_int_argument2_sensitive
#          - ge_int_arguments_sensitive
#          - ge_str_arguments
#          - ge_str_argument1_sensitive
#          - ge_str_argument2_sensitive
#          - ge_str_arguments_sensitive
#          - gt_int_arguments
#          - gt_int_argument1_sensitive
#          - gt_int_argument2_sensitive
#          - gt_int_arguments_sensitive
#          - gt_str_arguments
#          - gt_str_argument1_sensitive
#          - gt_str_argument2_sensitive
#          - gt_str_arguments_sensitive
#          - is_int_arguments
#          - is_int_argument1_sensitive
#          - is_int_argument2
#          - is_int_arguments_sensitive
#          - is_str_arguments
#          - is_str_argument1_sensitive
#          - is_str_argument2
#          - is_str_arguments_sensitive
#          - is_not_int_arguments
#          - is_not_int_argument1_sensitive
#          - is_not_int_argument2
#          - is_not_int_arguments_sensitive
#          - is_not_str_arguments
#          - is_not_str_argument1_sensitive
#          - is_not_str_argument2
#          - is_not_str_arguments_sensitive
#          - in_str_argument1
#          - in_str_argument2
#          - in_str_argument1_sensitive
#          - in_str_argument2_sensitive
#          - not_in_str_argument1
#          - not_in_str_argument2
#          - not_in_str_argument1_sensitive
#          - not_in_str_argument2_sensitive
#          - len_str_argument1
#          - len_str_argument2
#          - len_str_argument1_sensitive
#          - len_str_argument2_sensitive
#          - format1_int_arguments
#          - format1_int_argument1_sensitive
#          - format1_int_argument2_sensitive
#          - format1_int_arguments_sensitive
#          - format1_str_arguments
#          - format1_str_argument1_sensitive
#          - format1_str_argument2_sensitive
#          - format1_str_arguments_sensitive
#          - format2_int_arguments
#          - format2_int_argument1_sensitive
#          - format2_int_argument2_sensitive
#          - format2_int_arguments_sensitive
#          - format2_str_arguments
#          - format2_str_argument1_sensitive
#          - format2_str_argument2_sensitive
#          - format2_str_arguments_sensitive
#          - and_binary_arguments
#          - and_binary_argument1_sensitive
#          - and_binary_argument2_sensitive
#          - and_binary_arguments_sensitive
#          - or_binary_arguments
#          - or_binary_argument1_sensitive
#          - or_binary_argument2_sensitive
#          - or_binary_arguments_sensitive
#          - xor_binary_arguments
#          - xor_binary_argument1_sensitive
#          - xor_binary_argument2_sensitive
#          - xor_binary_arguments_sensitive
#          - not_binary_argument1
#          - not_binary_argument2
#          - not_binary_argument1_sensitive
#          - not_binary_argument2_sensitive
#          - lshift_binary_argument1
#          - lshift_binary_argument2
#          - lshift_binary_argument1_sensitive
#          - lshift_binary_argument2_sensitive
#          - rshift_binary_argument1
#          - rshift_binary_argument2
#          - rshift_binary_argument1_sensitive
#          - rshift_binary_argument2_sensitive
#          - hex_binary_argument1
#          - hex_binary_argument2
#          - hex_binary_argument1_sensitive
#          - hex_binary_argument2_sensitive
#          - oct_binary_argument1
#          - oct_binary_argument2
#          - oct_binary_argument1_sensitive
#          - oct_binary_argument2_sensitive
#          - int_float_argument1
#          - int_float_argument2
#          - int_float_argument1_sensitive
#          - int_float_argument2_sensitive
#          - long_float_argument1
#          - long_float_argument2
#          - long_float_argument1_sensitive
#          - long_float_argument2_sensitive
#          - float_int_argument1
#          - float_int_argument2
#          - float_int_argument1_sensitive
#          - float_int_argument2_sensitive
#          - chr_int_argument1
#          - chr_int_argument2
#          - chr_int_argument1_sensitive
#          - chr_int_argument2_sensitive
#          - unichr_int_argument1
#          - unichr_int_argument2
#          - unichr_int_argument1_sensitive
#          - unichr_int_argument2_sensitive
#          - bool_int_argument1
#          - bool_int_argument2
#          - bool_int_argument1_sensitive
#          - bool_int_argument2_sensitive
#          - str_int_argument1
#          - str_int_argument2
#          - str_int_argument1_sensitive
#          - str_int_argument2_sensitive
#          - str_float_argument1
#          - str_float_argument2
#          - str_float_argument1_sensitive
#          - str_float_argument2_sensitive
#          - unicode_int_argument1
#          - unicode_int_argument2
#          - unicode_int_argument1_sensitive
#          - unicode_int_argument2_sensitive
#          - unicode_float_argument1
#          - unicode_float_argument2
#          - unicode_float_argument1_sensitive
#          - unicode_float_argument2_sensitive
#          - unicode_str_argument1
#          - unicode_str_argument2
#          - unicode_str_argument1_sensitive
#          - unicode_str_argument2_sensitive
#          - trunc_float_argument1
#          - trunc_float_argument2
#          - trunc_float_argument1_sensitive
#          - trunc_float_argument2_sensitive
#          - range_int_argument1
#          - range_int_argument2
#          - range_int_argument1_sensitive
#          - range_int_argument2_sensitive
#          - pos_int_argument1
#          - pos_int_argument2
#          - pos_int_argument1_sensitive
#          - pos_int_argument2_sensitive
#          - pos_float_argument1
#          - pos_float_argument2
#          - pos_float_argument1_sensitive
#          - pos_float_argument2_sensitive
#          - neg_int_argument1
#          - neg_int_argument2
#          - neg_int_argument1_sensitive
#          - neg_int_argument2_sensitive
#          - neg_float_argument1
#          - neg_float_argument2
#          - neg_float_argument1_sensitive
#          - neg_float_argument2_sensitive
#          - abs_int_argument1
#          - abs_int_argument2
#          - abs_int_argument1_sensitive
#          - abs_int_argument2_sensitive
#          - abs_float_argument1
#          - abs_float_argument2
#          - abs_float_argument1_sensitive
#          - abs_float_argument2_sensitive
#          - round_int_argument1
#          - round_int_argument2
#          - round_int_argument1_sensitive
#          - round_int_argument2_sensitive
#          - round_float_argument1
#          - round_float_argument2
#          - round_float_argument1_sensitive
#          - round_float_argument2_sensitive
#          - pow1_int_argument1
#          - pow1_int_argument2
#          - pow1_int_argument1_sensitive
#          - pow1_int_argument2_sensitive
#          - pow1_float_argument1
#          - pow1_float_argument2
#          - pow1_float_argument1_sensitive
#          - pow1_float_argument2_sensitive
#          - pow2_int_argument1
#          - pow2_int_argument2
#          - pow2_int_argument1_sensitive
#          - pow2_int_argument2_sensitive
#          - pow2_float_argument1
#          - pow2_float_argument2
#          - pow2_float_argument1_sensitive
#          - pow2_float_argument2_sensitive
#          - max_int_arguments
#          - max_int_argument1_sensitive
#          - max_int_argument2_sensitive
#          - max_int_arguments_sensitive
#          - max_float_arguments
#          - max_float_argument1_sensitive
#          - max_float_argument2_sensitive
#          - max_float_arguments_sensitive
#          - max_str_arguments
#          - max_str_argument1_sensitive
#          - max_str_argument2_sensitive
#          - max_str_arguments_sensitive
#          - min_int_arguments
#          - min_int_argument1_sensitive
#          - min_int_argument2_sensitive
#          - min_int_arguments_sensitive
#          - min_float_arguments
#          - min_float_argument1_sensitive
#          - min_float_argument2_sensitive
#          - min_float_arguments_sensitive
#          - min_str_arguments
#          - min_str_argument1_sensitive
#          - min_str_argument2_sensitive
#          - min_str_arguments_sensitive
#          - add1_int_arguments
#          - add1_int_argument1_sensitive
#          - add1_int_argument2_sensitive
#          - add1_int_arguments_sensitive
#          - add1_str_arguments
#          - add1_str_argument1_sensitive
#          - add1_str_argument2_sensitive
#          - add1_str_arguments_sensitive
#          - add1_float_arguments
#          - add1_float_argument1_sensitive
#          - add1_float_argument2_sensitive
#          - add1_float_arguments_sensitive
#          - add2_int_arguments
#          - add2_int_argument1_sensitive
#          - add2_int_argument2_sensitive
#          - add2_int_arguments_sensitive
#          - add2_str_arguments
#          - add2_str_argument1_sensitive
#          - add2_str_argument2_sensitive
#          - add2_str_arguments_sensitive
#          - add2_float_arguments
#          - add2_float_argument1_sensitive
#          - add2_float_argument2_sensitive
#          - add2_float_arguments_sensitive
#          - sub_int_arguments
#          - sub_int_argument1_sensitive
#          - sub_int_argument2_sensitive
#          - sub_int_arguments_sensitive
#          - sub_float_arguments
#          - sub_float_argument1_sensitive
#          - sub_float_argument2_sensitive
#          - sub_float_arguments_sensitive
#          - mul_int_arguments
#          - mul_int_argument1_sensitive
#          - mul_int_argument2_sensitive
#          - mul_int_arguments_sensitive
#          - mul_float_arguments
#          - mul_float_argument1_sensitive
#          - mul_float_argument2_sensitive
#          - mul_float_arguments_sensitive
#          - div_int_arguments
#          - div_int_argument1_sensitive
#          - div_int_argument2_sensitive
#          - div_int_arguments_sensitive
#          - div_float_arguments
#          - div_float_argument1_sensitive
#          - div_float_argument2_sensitive
#          - div_float_arguments_sensitive
#          - truediv_int_arguments
#          - truediv_int_argument1_sensitive
#          - truediv_int_argument2_sensitive
#          - truediv_int_arguments_sensitive
#          - truediv_float_arguments
#          - truediv_float_argument1_sensitive
#          - truediv_float_argument2_sensitive
#          - truediv_float_arguments_sensitive
#          - floordiv_int_arguments
#          - floordiv_int_argument1_sensitive
#          - floordiv_int_argument2_sensitive
#          - floordiv_int_arguments_sensitive
#          - floordiv_float_arguments
#          - floordiv_float_argument1_sensitive
#          - floordiv_float_argument2_sensitive
#          - floordiv_float_arguments_sensitive
#          - mod_int_arguments
#          - mod_int_argument1_sensitive
#          - mod_int_argument2_sensitive
#          - mod_int_arguments_sensitive
#          - mod_float_arguments
#          - mod_float_argument1_sensitive
#          - mod_float_argument2_sensitive
#          - mod_float_arguments_sensitive
#          - divmod_int_argument1
#          - divmod_int_argument2
#          - divmod_int_argument1_sensitive
#          - divmod_int_argument2_sensitive
#          - divmod_float_argument1
#          - divmod_float_argument2
#          - divmod_float_argument1_sensitive
#          - divmod_float_argument2_sensitive
#          - repr_int_argument1
#          - repr_int_argument2
#          - repr_int_argument1_sensitive
#          - repr_int_argument2_sensitive
#          - repr_float_argument1
#          - repr_float_argument2
#          - repr_float_argument1_sensitive
#          - repr_float_argument2_sensitive
#          - repr_str_argument1
#          - repr_str_argument2
#          - repr_str_argument1_sensitive
#          - repr_str_argument2_sensitive
#          - index_list_argument1
#          - index_list_argument2
#          - index_list_argument1_sensitive
#          - index_list_argument2_sensitive
#          - sorted_list_argument1
#          - sorted_list_argument2
#          - sorted_list_argument1_sensitive
#          - sorted_list_argument2_sensitive
#          - sum_list_argument1
#          - sum_list_argument2
#          - sum_list_argument1_sensitive
#          - sum_list_argument2_sensitive
#          - tuple_list_argument1
#          - tuple_list_argument2
#          - tuple_list_argument1_sensitive
#          - tuple_list_argument2_sensitive
#          - type_int_argument1
#          - type_int_argument2
#          - type_int_argument1_sensitive
#          - type_int_argument2_sensitive
#          - type_float_argument1
#          - type_float_argument2
#          - type_float_argument1_sensitive
#          - type_float_argument2_sensitive
#          - type_str_argument1
#          - type_str_argument2
#          - type_str_argument1_sensitive
#          - type_str_argument2_sensitive
#          - type_list_argument1
#          - type_list_argument2
#          - type_list_argument1_sensitive
#          - type_list_argument2_sensitive

#  outputs:
#    - cmp_int_arguments
#    - cmp_int_argument1_sensitive
#    - cmp_int_argument2_sensitive
#    - cmp_int_arguments_sensitive
#    - cmp_str_arguments
#    - cmp_str_argument1_sensitive
#    - cmp_str_argument2_sensitive
#    - cmp_str_arguments_sensitive
#    - eq_int_arguments
#    - eq_int_argument1_sensitive
#    - eq_int_argument2_sensitive
#    - eq_int_arguments_sensitive
#    - eq_str_arguments
#    - eq_str_argument1_sensitive
#    - eq_str_argument2_sensitive
#    - eq_str_arguments_sensitive
#    - ne1_int_arguments
#    - ne1_int_argument1_sensitive
#    - ne1_int_argument2_sensitive
#    - ne1_int_arguments_sensitive
#    - ne1_str_arguments
#    - ne1_str_argument1_sensitive
#    - ne1_str_argument2_sensitive
#    - ne1_str_arguments_sensitive
#    - ne2_int_arguments
#    - ne2_int_argument1_sensitive
#    - ne2_int_argument2_sensitive
#    - ne2_int_arguments_sensitive
#    - ne2_str_arguments
#    - ne2_str_argument1_sensitive
#    - ne2_str_argument2_sensitive
#    - ne2_str_arguments_sensitive
#    - le_int_arguments
#    - le_int_argument1_sensitive
#    - le_int_argument2_sensitive
#    - le_int_arguments_sensitive
#    - le_str_arguments
#    - le_str_argument1_sensitive
#    - le_str_argument2_sensitive
#    - le_str_arguments_sensitive
#    - lt_int_arguments
#    - lt_int_argument1_sensitive
#    - lt_int_argument2_sensitive
#    - lt_int_arguments_sensitive
#    - lt_str_arguments
#    - lt_str_argument1_sensitive
#    - lt_str_argument2_sensitive
#    - lt_str_arguments_sensitive
#    - ge_int_arguments
#    - ge_int_argument1_sensitive
#    - ge_int_argument2_sensitive
#    - ge_int_arguments_sensitive
#    - ge_str_arguments
#    - ge_str_argument1_sensitive
#    - ge_str_argument2_sensitive
#    - ge_str_arguments_sensitive
#    - gt_int_arguments
#    - gt_int_argument1_sensitive
#    - gt_int_argument2_sensitive
#    - gt_int_arguments_sensitive
#    - gt_str_arguments
#    - gt_str_argument1_sensitive
#    - gt_str_argument2_sensitive
#    - gt_str_arguments_sensitive
#    - is_int_arguments
#    - is_int_argument1_sensitive
#    - is_int_argument2
#    - is_int_arguments_sensitive
#    - is_str_arguments
#    - is_str_argument1_sensitive
#    - is_str_argument2
#    - is_str_arguments_sensitive
#    - is_not_int_arguments
#    - is_not_int_argument1_sensitive
#    - is_not_int_argument2
#    - is_not_int_arguments_sensitive
#    - is_not_str_arguments
#    - is_not_str_argument1_sensitive
#    - is_not_str_argument2
#    - is_not_str_arguments_sensitive
#    - in_str_argument1
#    - in_str_argument2
#    - in_str_argument1_sensitive
#    - in_str_argument2_sensitive
#    - not_in_str_argument1
#    - not_in_str_argument2
#    - not_in_str_argument1_sensitive
#    - not_in_str_argument2_sensitive
#    - len_str_argument1
#    - len_str_argument2
#    - len_str_argument1_sensitive
#    - len_str_argument2_sensitive
#    - format1_int_arguments
#    - format1_int_argument1_sensitive
#    - format1_int_argument2_sensitive
#    - format1_int_arguments_sensitive
#    - format1_str_arguments
#    - format1_str_argument1_sensitive
#    - format1_str_argument2_sensitive
#    - format1_str_arguments_sensitive
#    - format2_int_arguments
#    - format2_int_argument1_sensitive
#    - format2_int_argument2_sensitive
#    - format2_int_arguments_sensitive
#    - format2_str_arguments
#    - format2_str_argument1_sensitive
#    - format2_str_argument2_sensitive
#    - format2_str_arguments_sensitive
#    - and_binary_arguments
#    - and_binary_argument1_sensitive
#    - and_binary_argument2_sensitive
#    - and_binary_arguments_sensitive
#    - or_binary_arguments
#    - or_binary_argument1_sensitive
#    - or_binary_argument2_sensitive
#    - or_binary_arguments_sensitive
#    - xor_binary_arguments
#    - xor_binary_argument1_sensitive
#    - xor_binary_argument2_sensitive
#    - xor_binary_arguments_sensitive
#    - not_binary_argument1
#    - not_binary_argument2
#    - not_binary_argument1_sensitive
#    - not_binary_argument2_sensitive
#    - lshift_binary_argument1
#    - lshift_binary_argument2
#    - lshift_binary_argument1_sensitive
#    - lshift_binary_argument2_sensitive
#    - rshift_binary_argument1
#    - rshift_binary_argument2
#    - rshift_binary_argument1_sensitive
#    - rshift_binary_argument2_sensitive
#    - hex_binary_argument1
#    - hex_binary_argument2
#    - hex_binary_argument1_sensitive
#    - hex_binary_argument2_sensitive
#    - oct_binary_argument1
#    - oct_binary_argument2
#    - oct_binary_argument1_sensitive
#    - oct_binary_argument2_sensitive
#    - int_float_argument1
#    - int_float_argument2
#    - int_float_argument1_sensitive
#    - int_float_argument2_sensitive
#    - long_float_argument1
#    - long_float_argument2
#    - long_float_argument1_sensitive
#    - long_float_argument2_sensitive
#    - float_int_argument1
#    - float_int_argument2
#    - float_int_argument1_sensitive
#    - float_int_argument2_sensitive
#    - chr_int_argument1
#    - chr_int_argument2
#    - chr_int_argument1_sensitive
#    - chr_int_argument2_sensitive
#    - unichr_int_argument1
#    - unichr_int_argument2
#    - unichr_int_argument1_sensitive
#    - unichr_int_argument2_sensitive
#    - bool_int_argument1
#    - bool_int_argument2
#    - bool_int_argument1_sensitive
#    - bool_int_argument2_sensitive
#    - str_int_argument1
#    - str_int_argument2
#    - str_int_argument1_sensitive
#    - str_int_argument2_sensitive
#    - str_float_argument1
#    - str_float_argument2
#    - str_float_argument1_sensitive
#    - str_float_argument2_sensitive
#    - unicode_int_argument1
#    - unicode_int_argument2
#    - unicode_int_argument1_sensitive
#    - unicode_int_argument2_sensitive
#    - unicode_float_argument1
#    - unicode_float_argument2
#    - unicode_float_argument1_sensitive
#    - unicode_float_argument2_sensitive
#    - unicode_str_argument1
#    - unicode_str_argument2
#    - unicode_str_argument1_sensitive
#    - unicode_str_argument2_sensitive
#    - trunc_float_argument1
#    - trunc_float_argument2
#    - trunc_float_argument1_sensitive
#    - trunc_float_argument2_sensitive
#    - range_int_argument1
#    - range_int_argument2
#    - range_int_argument1_sensitive
#    - range_int_argument2_sensitive
#    - pos_int_argument1
#    - pos_int_argument2
#    - pos_int_argument1_sensitive
#    - pos_int_argument2_sensitive
#    - pos_float_argument1
#    - pos_float_argument2
#    - pos_float_argument1_sensitive
#    - pos_float_argument2_sensitive
#    - neg_int_argument1
#    - neg_int_argument2
#    - neg_int_argument1_sensitive
#    - neg_int_argument2_sensitive
#    - neg_float_argument1
#    - neg_float_argument2
#    - neg_float_argument1_sensitive
#    - neg_float_argument2_sensitive
#    - abs_int_argument1
#    - abs_int_argument2
#    - abs_int_argument1_sensitive
#    - abs_int_argument2_sensitive
#    - abs_float_argument1
#    - abs_float_argument2
#    - abs_float_argument1_sensitive
#    - abs_float_argument2_sensitive
#    - round_int_argument1
#    - round_int_argument2
#    - round_int_argument1_sensitive
#    - round_int_argument2_sensitive
#    - round_float_argument1
#    - round_float_argument2
#    - round_float_argument1_sensitive
#    - round_float_argument2_sensitive
#    - pow1_int_argument1
#    - pow1_int_argument2
#    - pow1_int_argument1_sensitive
#    - pow1_int_argument2_sensitive
#    - pow1_float_argument1
#    - pow1_float_argument2
#    - pow1_float_argument1_sensitive
#    - pow1_float_argument2_sensitive
#    - pow2_int_argument1
#    - pow2_int_argument2
#    - pow2_int_argument1_sensitive
#    - pow2_int_argument2_sensitive
#    - pow2_float_argument1
#    - pow2_float_argument2
#    - pow2_float_argument1_sensitive
#    - pow2_float_argument2_sensitive
#    - max_int_arguments
#    - max_int_argument1_sensitive
#    - max_int_argument2_sensitive
#    - max_int_arguments_sensitive
#    - max_float_arguments
#    - max_float_argument1_sensitive
#    - max_float_argument2_sensitive
#    - max_float_arguments_sensitive
#    - max_str_arguments
#    - max_str_argument1_sensitive
#    - max_str_argument2_sensitive
#    - max_str_arguments_sensitive
#    - min_int_arguments
#    - min_int_argument1_sensitive
#    - min_int_argument2_sensitive
#    - min_int_arguments_sensitive
#    - min_float_arguments
#    - min_float_argument1_sensitive
#    - min_float_argument2_sensitive
#    - min_float_arguments_sensitive
#    - min_str_arguments
#    - min_str_argument1_sensitive
#    - min_str_argument2_sensitive
#    - min_str_arguments_sensitive
#    - add1_int_arguments
#    - add1_int_argument1_sensitive
#    - add1_int_argument2_sensitive
#    - add1_int_arguments_sensitive
#    - add1_str_arguments
#    - add1_str_argument1_sensitive
#    - add1_str_argument2_sensitive
#    - add1_str_arguments_sensitive
#    - add1_float_arguments
#    - add1_float_argument1_sensitive
#    - add1_float_argument2_sensitive
#    - add1_float_arguments_sensitive
#    - add2_int_arguments
#    - add2_int_argument1_sensitive
#    - add2_int_argument2_sensitive
#    - add2_int_arguments_sensitive
#    - add2_str_arguments
#    - add2_str_argument1_sensitive
#    - add2_str_argument2_sensitive
#    - add2_str_arguments_sensitive
#    - add2_float_arguments
#    - add2_float_argument1_sensitive
#    - add2_float_argument2_sensitive
#    - add2_float_arguments_sensitive
#    - sub_int_arguments
#    - sub_int_argument1_sensitive
#    - sub_int_argument2_sensitive
#    - sub_int_arguments_sensitive
#    - sub_float_arguments
#    - sub_float_argument1_sensitive
#    - sub_float_argument2_sensitive
#    - sub_float_arguments_sensitive
#    - mul_int_arguments
#    - mul_int_argument1_sensitive
#    - mul_int_argument2_sensitive
#    - mul_int_arguments_sensitive
#    - mul_float_arguments
#    - mul_float_argument1_sensitive
#    - mul_float_argument2_sensitive
#    - mul_float_arguments_sensitive
#    - div_int_arguments
#    - div_int_argument1_sensitive
#    - div_int_argument2_sensitive
#    - div_int_arguments_sensitive
#    - div_float_arguments
#    - div_float_argument1_sensitive
#    - div_float_argument2_sensitive
#    - div_float_arguments_sensitive
#    - truediv_int_arguments
#    - truediv_int_argument1_sensitive
#    - truediv_int_argument2_sensitive
#    - truediv_int_arguments_sensitive
#    - truediv_float_arguments
#    - truediv_float_argument1_sensitive
#    - truediv_float_argument2_sensitive
#    - truediv_float_arguments_sensitive
#    - floordiv_int_arguments
#    - floordiv_int_argument1_sensitive
#    - floordiv_int_argument2_sensitive
#    - floordiv_int_arguments_sensitive
#    - floordiv_float_arguments
#    - floordiv_float_argument1_sensitive
#    - floordiv_float_argument2_sensitive
#    - floordiv_float_arguments_sensitive
#    - mod_int_arguments
#    - mod_int_argument1_sensitive
#    - mod_int_argument2_sensitive
#    - mod_int_arguments_sensitive
#    - mod_float_arguments
#    - mod_float_argument1_sensitive
#    - mod_float_argument2_sensitive
#    - mod_float_arguments_sensitive
#    - divmod_int_argument1
#    - divmod_int_argument2
#    - divmod_int_argument1_sensitive
#    - divmod_int_argument2_sensitive
#    - divmod_float_argument1
#    - divmod_float_argument2
#    - divmod_float_argument1_sensitive
#    - divmod_float_argument2_sensitive
#    - repr_int_argument1
#    - repr_int_argument2
#    - repr_int_argument1_sensitive
#    - repr_int_argument2_sensitive
#    - repr_float_argument1
#    - repr_float_argument2
#    - repr_float_argument1_sensitive
#    - repr_float_argument2_sensitive
#    - repr_str_argument1
#    - repr_str_argument2
#    - repr_str_argument1_sensitive
#    - repr_str_argument2_sensitive
#    - index_list_argument1
#    - index_list_argument2
#    - index_list_argument1_sensitive
#    - index_list_argument2_sensitive
#    - sorted_list_argument1
#    - sorted_list_argument2
#    - sorted_list_argument1_sensitive
#    - sorted_list_argument2_sensitive
#    - sum_list_argument1
#    - sum_list_argument2
#    - sum_list_argument1_sensitive
#    - sum_list_argument2_sensitive
#    - tuple_list_argument1
#    - tuple_list_argument2
#    - tuple_list_argument1_sensitive
#    - tuple_list_argument2_sensitive
#    - type_int_argument1
#    - type_int_argument2
#    - type_int_argument1_sensitive
#    - type_int_argument2_sensitive
#    - type_float_argument1
#    - type_float_argument2
#    - type_float_argument1_sensitive
#    - type_float_argument2_sensitive
#    - type_str_argument1
#    - type_str_argument2
#    - type_str_argument1_sensitive
#    - type_str_argument2_sensitive
#    - type_list_argument1
#    - type_list_argument2
#    - type_list_argument1_sensitive
#    - type_list_argument2_sensitive
        navigate:
          - SUCCESS: SUCCESS

  results:
    - SUCCESS
