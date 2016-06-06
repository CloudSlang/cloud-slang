#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: sensitive_values_in_python_expressions_op

  inputs:
    - int1_input: 3
    - int1_input_sensitive:
        default: 3
        sensitive: true

    - int2_input: 4
    - int2_input_sensitive:
        default: 4
        sensitive: true

    - str1_input: 'ab'
    - str1_input_sensitive:
        default: 'ab'
        sensitive: true

    - str2_input: 'bc'
    - str2_input_sensitive:
        default: 'bc'
        sensitive: true

    - float1_input: -2.5
    - float1_input_sensitive:
        default: -2.5
        sensitive: true

    - float2_input: 11.4
    - float2_input_sensitive:
        default: 11.4
        sensitive: true

    - binary1_input: 0b00111100
    - binary1_input_sensitive:
        default: 0b00111100
        sensitive: true

    - binary2_input: 0b00001101
    - binary2_input_sensitive:
        default: 0b00001101
        sensitive: true

    - list1_input: ${[1, 2, 3]}
    - list1_input_sensitive:
        default: ${[1, 2, 3]}
        sensitive: true

    - list2_input: ${[5, 10, 15]}
    - list2_input_sensitive:
        default: ${[5, 10, 15]}
        sensitive: true

  python_action:
    script: |
      print int1_input
      print int1_input_sensitive
      print int2_input
      print int2_input_sensitive
      print str1_input
      print str1_input_sensitive
      print str2_input
      print str2_input_sensitive
      print float1_input
      print float1_input_sensitive
      print float2_input
      print float2_input_sensitive
      print binary1_input
      print binary1_input_sensitive
      print binary2_input
      print binary2_input_sensitive
      print list1_input
      print list1_input_sensitive
      print list2_input
      print list2_input_sensitive

  outputs:
    - cmp_int_arguments: ${int1_input == int2_input}
    - cmp_sensitive_int_arguments:
        value: ${int1_input == int2_input}
        sensitive: true
    - cmp_int_argument1_sensitive: ${int1_input_sensitive == int2_input}
    - cmp_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive == int2_input}
        sensitive: true
    - cmp_int_argument2_sensitive: ${int1_input == int2_input_sensitive}
    - cmp_sensitive_int_argument2_sensitive:
        value: ${int1_input == int2_input_sensitive}
        sensitive: true
    - cmp_int_arguments_sensitive: ${int1_input_sensitive == int2_input_sensitive}
    - cmp_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive == int2_input_sensitive}
        sensitive: true

    - cmp_str_arguments: ${str1_input == str2_input}
    - cmp_sensitive_str_arguments:
        value: ${str1_input == str2_input}
        sensitive: true
    - cmp_str_argument1_sensitive: ${str1_input_sensitive == str2_input}
    - cmp_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive == str2_input}
        sensitive: true
    - cmp_str_argument2_sensitive: ${str1_input == str2_input_sensitive}
    - cmp_sensitive_str_argument2_sensitive:
        value: ${str1_input == str2_input_sensitive}
        sensitive: true
    - cmp_str_arguments_sensitive: ${str1_input_sensitive == str2_input_sensitive}
    - cmp_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive == str2_input_sensitive}
        sensitive: true

    - eq_int_arguments: ${int1_input == int2_input}
    - eq_sensitive_int_arguments:
        value: ${int1_input == int2_input}
        sensitive: true
    - eq_int_argument1_sensitive: ${int1_input_sensitive == int2_input}
    - eq_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive == int2_input}
        sensitive: true
    - eq_int_argument2_sensitive: ${int1_input == int2_input_sensitive}
    - eq_sensitive_int_argument2_sensitive:
        value: ${int1_input == int2_input_sensitive}
        sensitive: true
    - eq_int_arguments_sensitive: ${int1_input_sensitive == int2_input_sensitive}
    - eq_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive == int2_input_sensitive}
        sensitive: true

    - eq_str_arguments: ${str1_input == str2_input}
    - eq_sensitive_str_arguments:
        value: ${str1_input == str2_input}
        sensitive: true
    - eq_str_argument1_sensitive: ${str1_input_sensitive == str2_input}
    - eq_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive == str2_input}
        sensitive: true
    - eq_str_argument2_sensitive: ${str1_input == str2_input_sensitive}
    - eq_sensitive_str_argument2_sensitive:
        value: ${str1_input == str2_input_sensitive}
        sensitive: true
    - eq_str_arguments_sensitive: ${str1_input_sensitive == str2_input_sensitive}
    - eq_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive == str2_input_sensitive}
        sensitive: true

    - ne1_int_arguments: ${int1_input != int2_input}
    - ne1_sensitive_int_arguments:
        value: ${int1_input != int2_input}
        sensitive: true
    - ne1_int_argument1_sensitive: ${int1_input_sensitive != int2_input}
    - ne1_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive != int2_input}
        sensitive: true
    - ne1_int_argument2_sensitive: ${int1_input != int2_input_sensitive}
    - ne1_sensitive_int_argument2_sensitive:
        value: ${int1_input != int2_input_sensitive}
        sensitive: true
    - ne1_int_arguments_sensitive: ${int1_input_sensitive != int2_input_sensitive}
    - ne1_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive != int2_input_sensitive}
        sensitive: true

    - ne1_str_arguments: ${str1_input != str2_input}
    - ne1_sensitive_str_arguments:
        value: ${str1_input != str2_input}
        sensitive: true
    - ne1_str_argument1_sensitive: ${str1_input_sensitive != str2_input}
    - ne1_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive != str2_input}
        sensitive: true
    - ne1_str_argument2_sensitive: ${str1_input != str2_input_sensitive}
    - ne1_sensitive_str_argument2_sensitive:
        value: ${str1_input != str2_input_sensitive}
        sensitive: true
    - ne1_str_arguments_sensitive: ${str1_input_sensitive != str2_input_sensitive}
    - ne1_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive != str2_input_sensitive}
        sensitive: true

    - ne2_int_arguments: ${int1_input <> int2_input}
    - ne2_sensitive_int_arguments:
        value: ${int1_input <> int2_input}
        sensitive: true
    - ne2_int_argument1_sensitive: ${int1_input_sensitive <> int2_input}
    - ne2_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive <> int2_input}
        sensitive: true
    - ne2_int_argument2_sensitive: ${int1_input <> int2_input_sensitive}
    - ne2_sensitive_int_argument2_sensitive:
        value: ${int1_input <> int2_input_sensitive}
        sensitive: true
    - ne2_int_arguments_sensitive: ${int1_input_sensitive <> int2_input_sensitive}
    - ne2_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive <> int2_input_sensitive}
        sensitive: true

    - ne2_str_arguments: ${str1_input <> str2_input}
    - ne2_sensitive_str_arguments:
        value: ${str1_input <> str2_input}
        sensitive: true
    - ne2_str_argument1_sensitive: ${str1_input_sensitive <> str2_input}
    - ne2_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive <> str2_input}
        sensitive: true
    - ne2_str_argument2_sensitive: ${str1_input <> str2_input_sensitive}
    - ne2_sensitive_str_argument2_sensitive:
        value: ${str1_input <> str2_input_sensitive}
        sensitive: true
    - ne2_str_arguments_sensitive: ${str1_input_sensitive <> str2_input_sensitive}
    - ne2_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive <> str2_input_sensitive}
        sensitive: true

    - le_int_arguments: ${int1_input <= int2_input}
    - le_sensitive_int_arguments:
        value: ${int1_input <= int2_input}
        sensitive: true
    - le_int_argument1_sensitive: ${int1_input_sensitive <= int2_input}
    - le_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive <= int2_input}
        sensitive: true
    - le_int_argument2_sensitive: ${int1_input <= int2_input_sensitive}
    - le_sensitive_int_argument2_sensitive:
        value: ${int1_input <= int2_input_sensitive}
        sensitive: true
    - le_int_arguments_sensitive: ${int1_input_sensitive <= int2_input_sensitive}
    - le_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive <= int2_input_sensitive}
        sensitive: true

    - le_str_arguments: ${str1_input <= str2_input}
    - le_sensitive_str_arguments:
        value: ${str1_input <= str2_input}
        sensitive: true
    - le_str_argument1_sensitive: ${str1_input_sensitive <= str2_input}
    - le_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive <= str2_input}
        sensitive: true
    - le_str_argument2_sensitive: ${str1_input <= str2_input_sensitive}
    - le_sensitive_str_argument2_sensitive:
        value: ${str1_input <= str2_input_sensitive}
        sensitive: true
    - le_str_arguments_sensitive: ${str1_input_sensitive <= str2_input_sensitive}
    - le_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive <= str2_input_sensitive}
        sensitive: true

    - lt_int_arguments: ${int1_input < int2_input}
    - lt_sensitive_int_arguments:
        value: ${int1_input < int2_input}
        sensitive: true
    - lt_int_argument1_sensitive: ${int1_input_sensitive < int2_input}
    - lt_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive < int2_input}
        sensitive: true
    - lt_int_argument2_sensitive: ${int1_input < int2_input_sensitive}
    - lt_sensitive_int_argument2_sensitive:
        value: ${int1_input < int2_input_sensitive}
        sensitive: true
    - lt_int_arguments_sensitive: ${int1_input_sensitive < int2_input_sensitive}
    - lt_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive < int2_input_sensitive}
        sensitive: true

    - lt_str_arguments: ${str1_input < str2_input}
    - lt_sensitive_str_arguments:
        value: ${str1_input < str2_input}
        sensitive: true
    - lt_str_argument1_sensitive: ${str1_input_sensitive < str2_input}
    - lt_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive < str2_input}
        sensitive: true
    - lt_str_argument2_sensitive: ${str1_input < str2_input_sensitive}
    - lt_sensitive_str_argument2_sensitive:
        value: ${str1_input < str2_input_sensitive}
        sensitive: true
    - lt_str_arguments_sensitive: ${str1_input_sensitive < str2_input_sensitive}
    - lt_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive < str2_input_sensitive}
        sensitive: true

    - ge_int_arguments: ${int1_input >= int2_input}
    - ge_sensitive_int_arguments:
        value: ${int1_input >= int2_input}
        sensitive: true
    - ge_int_argument1_sensitive: ${int1_input_sensitive >= int2_input}
    - ge_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive >= int2_input}
        sensitive: true
    - ge_int_argument2_sensitive: ${int1_input >= int2_input_sensitive}
    - ge_sensitive_int_argument2_sensitive:
        value: ${int1_input >= int2_input_sensitive}
        sensitive: true
    - ge_int_arguments_sensitive: ${int1_input_sensitive >= int2_input_sensitive}
    - ge_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive >= int2_input_sensitive}
        sensitive: true

    - ge_str_arguments: ${str1_input >= str2_input}
    - ge_sensitive_str_arguments:
        value: ${str1_input >= str2_input}
        sensitive: true
    - ge_str_argument1_sensitive: ${str1_input_sensitive >= str2_input}
    - ge_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive >= str2_input}
        sensitive: true
    - ge_str_argument2_sensitive: ${str1_input >= str2_input_sensitive}
    - ge_sensitive_str_argument2_sensitive:
        value: ${str1_input >= str2_input_sensitive}
        sensitive: true
    - ge_str_arguments_sensitive: ${str1_input_sensitive >= str2_input_sensitive}
    - ge_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive >= str2_input_sensitive}
        sensitive: true

    - gt_int_arguments: ${int1_input > int2_input}
    - gt_sensitive_int_arguments:
        value: ${int1_input > int2_input}
        sensitive: true
    - gt_int_argument1_sensitive: ${int1_input_sensitive > int2_input}
    - gt_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive > int2_input}
        sensitive: true
    - gt_int_argument2_sensitive: ${int1_input > int2_input_sensitive}
    - gt_sensitive_int_argument2_sensitive:
        value: ${int1_input > int2_input_sensitive}
        sensitive: true
    - gt_int_arguments_sensitive: ${int1_input_sensitive > int2_input_sensitive}
    - gt_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive > int2_input_sensitive}
        sensitive: true

    - gt_str_arguments: ${str1_input > str2_input}
    - gt_sensitive_str_arguments:
        value: ${str1_input > str2_input}
        sensitive: true
    - gt_str_argument1_sensitive: ${str1_input_sensitive > str2_input}
    - gt_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive > str2_input}
        sensitive: true
    - gt_str_argument2_sensitive: ${str1_input > str2_input_sensitive}
    - gt_sensitive_str_argument2_sensitive:
        value: ${str1_input > str2_input_sensitive}
        sensitive: true
    - gt_str_arguments_sensitive: ${str1_input_sensitive > str2_input_sensitive}
    - gt_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive > str2_input_sensitive}
        sensitive: true

    - is_int_arguments: ${int1_input is int2_input}
    - is_sensitive_int_arguments:
        value: ${int1_input is int2_input}
        sensitive: true
    - is_int_argument1_sensitive: ${int1_input_sensitive is int2_input}
    - is_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive is int2_input}
        sensitive: true
    - is_int_argument2_sensitive: ${int1_input is int2_input_sensitive}
    - is_sensitive_int_argument2_sensitive:
        value: ${int1_input is int2_input_sensitive}
        sensitive: true
    - is_int_arguments_sensitive: ${int1_input_sensitive is int2_input_sensitive}
    - is_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive is int2_input_sensitive}
        sensitive: true

    - is_str_arguments: ${str1_input is str2_input}
    - is_sensitive_str_arguments:
        value: ${str1_input is str2_input}
        sensitive: true
    - is_str_argument1_sensitive: ${str1_input_sensitive is str2_input}
    - is_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive is str2_input}
        sensitive: true
    - is_str_argument2_sensitive: ${str1_input is str2_input_sensitive}
    - is_sensitive_str_argument2_sensitive:
        value: ${str1_input is str2_input_sensitive}
        sensitive: true
    - is_str_arguments_sensitive: ${str1_input_sensitive is str2_input_sensitive}
    - is_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive is str2_input_sensitive}
        sensitive: true

    - is_not_int_arguments: ${int1_input is not int2_input}
    - is_sensitive_not_int_arguments:
        value: ${int1_input is not int2_input}
        sensitive: true
    - is_not_int_argument1_sensitive: ${int1_input_sensitive is not int2_input}
    - is_sensitive_not_int_argument1_sensitive:
        value: ${int1_input_sensitive is not int2_input}
        sensitive: true
    - is_not_int_argument2_sensitive: ${int1_input is not int2_input_sensitive}
    - is_sensitive_not_int_argument2_sensitive:
        value: ${int1_input is not int2_input_sensitive}
        sensitive: true
    - is_not_int_arguments_sensitive: ${int1_input_sensitive is not int2_input_sensitive}
    - is_sensitive_not_int_arguments_sensitive:
        value: ${int1_input_sensitive is not int2_input_sensitive}
        sensitive: true

    - is_not_str_arguments: ${str1_input is not str2_input}
    - is_sensitive_not_str_arguments:
        value: ${str1_input is not str2_input}
        sensitive: true
    - is_not_str_argument1_sensitive: ${str1_input_sensitive is not str2_input}
    - is_sensitive_not_str_argument1_sensitive:
        value: ${str1_input_sensitive is not str2_input}
        sensitive: true
    - is_not_str_argument2_sensitive: ${str1_input is not str2_input_sensitive}
    - is_sensitive_not_str_argument2_sensitive:
        value: ${str1_input is not str2_input_sensitive}
        sensitive: true
    - is_not_str_arguments_sensitive: ${str1_input_sensitive is not str2_input_sensitive}
    - is_sensitive_not_str_arguments_sensitive:
        value: ${str1_input_sensitive is not str2_input_sensitive}
        sensitive: true

    - in_str_argument1: ${str1_input in 'abcd'}
    - in_sensitive_str_argument1:
        value: ${str1_input in 'abcd'}
        sensitive: true
    - in_str_argument2: ${str2_input in 'abcd'}
    - in_sensitive_str_argument2:
        value: ${str2_input in 'abcd'}
        sensitive: true
    - in_str_argument1_sensitive: ${str1_input_sensitive in 'abcd'}
    - in_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive in 'abcd'}
        sensitive: true
    - in_str_argument2_sensitive: ${str2_input_sensitive in 'abcd'}
    - in_sensitive_str_argument2_sensitive:
        value: ${str2_input_sensitive in 'abcd'}
        sensitive: true

    - not_in_str_argument1: ${str1_input not in 'abcd'}
    - not_in_sensitive_str_argument1:
        value: ${str1_input not in 'abcd'}
        sensitive: true
    - not_in_str_argument2: ${str2_input not in 'abcd'}
    - not_in_sensitive_str_argument2:
        value: ${str2_input not in 'abcd'}
        sensitive: true
    - not_in_str_argument1_sensitive: ${str1_input_sensitive not in 'abcd'}
    - not_in_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive not in 'abcd'}
        sensitive: true
    - not_in_str_argument2_sensitive: ${str2_input_sensitive not in 'abcd'}
    - not_in_sensitive_str_argument2_sensitive:
        value: ${str2_input_sensitive not in 'abcd'}
        sensitive: true

    - len_str_argument1: ${len(str1_input)}
    - len_sensitive_str_argument1:
        value: ${len(str1_input)}
        sensitive: true
    - len_str_argument2: ${len(str2_input)}
    - len_sensitive_str_argument2:
        value: ${len(str2_input)}
        sensitive: true
    - len_str_argument1_sensitive: ${len(str1_input_sensitive)}
    - len_sensitive_str_argument1_sensitive:
        value: ${len(str1_input_sensitive)}
        sensitive: true
    - len_str_argument2_sensitive: ${len(str2_input_sensitive)}
    - len_sensitive_str_argument2_sensitive:
        value: ${len(str2_input_sensitive)}
        sensitive: true

    - format1_int_arguments: ${"%d and %d" %(int1_input,int2_input)}
    - format1_sensitive_int_arguments:
        value: ${"%d and %d" %(int1_input,int2_input)}
        sensitive: true
    - format1_int_argument1_sensitive: ${"%d and %d" %(int1_input_sensitive,int2_input)}
    - format1_sensitive_int_argument1_sensitive:
        value: ${"%d and %d" %(int1_input_sensitive,int2_input)}
        sensitive: true
    - format1_int_argument2_sensitive: ${"%d and %d" %(int1_input,int2_input_sensitive)}
    - format1_sensitive_int_argument2_sensitive:
        value: ${"%d and %d" %(int1_input,int2_input_sensitive)}
        sensitive: true
    - format1_int_arguments_sensitive: ${"%d and %d" %(int1_input_sensitive,int2_input_sensitive)}
    - format1_sensitive_int_arguments_sensitive:
        value: ${"%d and %d" %(int1_input_sensitive,int2_input_sensitive)}
        sensitive: true

    - format1_str_arguments: ${"%s and %s" %(str1_input,str2_input)}
    - format1_sensitive_str_arguments:
        value: ${"%s and %s" %(str1_input,str2_input)}
        sensitive: true
    - format1_str_argument1_sensitive: ${"%s and %s" %(str1_input_sensitive,str2_input)}
    - format1_sensitive_str_argument1_sensitive:
        value: ${"%s and %s" %(str1_input_sensitive,str2_input)}
        sensitive: true
    - format1_str_argument2_sensitive: ${"%s and %s" %(str1_input,str2_input_sensitive)}
    - format1_sensitive_str_argument2_sensitive:
        value: ${"%s and %s" %(str1_input,str2_input_sensitive)}
        sensitive: true
    - format1_str_arguments_sensitive: ${"%s and %s" %(str1_input_sensitive,str2_input_sensitive)}
    - format1_sensitive_str_arguments_sensitive:
        value: ${"%s and %s" %(str1_input_sensitive,str2_input_sensitive)}
        sensitive: true

    - format2_int_arguments: ${"%{0} and {1}".format(int1_input,int2_input)}
    - format2_sensitive_int_arguments:
        value: ${"%{0} and {1}".format(int1_input,int2_input)}
        sensitive: true
    - format2_int_argument1_sensitive: ${"%{0} and {1}".format(int1_input_sensitive,int2_input)}
    - format2_sensitive_int_argument1_sensitive:
        value: ${"%{0} and {1}".format(int1_input_sensitive,int2_input)}
        sensitive: true
    - format2_int_argument2_sensitive: ${"%{0} and {1}".format(int1_input,int2_input_sensitive)}
    - format2_sensitive_int_argument2_sensitive:
        value: ${"%{0} and {1}".format(int1_input,int2_input_sensitive)}
        sensitive: true
    - format2_int_arguments_sensitive: ${"%{0} and {1}".format(int1_input_sensitive,int2_input_sensitive)}
    - format2_sensitive_int_arguments_sensitive:
        value: ${"%{0} and {1}".format(int1_input_sensitive,int2_input_sensitive)}
        sensitive: true

    - format2_str_arguments: ${"%{0} and {1}".format(str1_input,str2_input)}
    - format2_sensitive_str_arguments:
        value: ${"%{0} and {1}".format(str1_input,str2_input)}
        sensitive: true
    - format2_str_argument1_sensitive: ${"%{0} and {1}".format(str1_input_sensitive,str2_input)}
    - format2_sensitive_str_argument1_sensitive:
        value: ${"%{0} and {1}".format(str1_input_sensitive,str2_input)}
        sensitive: true
    - format2_str_argument2_sensitive: ${"%{0} and {1}".format(str1_input,str2_input_sensitive)}
    - format2_sensitive_str_argument2_sensitive:
        value: ${"%{0} and {1}".format(str1_input,str2_input_sensitive)}
        sensitive: true
    - format2_str_arguments_sensitive: ${"%{0} and {1}".format(str1_input_sensitive,str2_input_sensitive)}
    - format2_sensitive_str_arguments_sensitive:
        value: ${"%{0} and {1}".format(str1_input_sensitive,str2_input_sensitive)}
        sensitive: true

    - and_binary_arguments: ${binary1_input & binary2_input}
    - and_sensitive_binary_arguments:
        value: ${binary1_input & binary2_input}
        sensitive: true
    - and_binary_argument1_sensitive: ${binary1_input_sensitive & binary2_input}
    - and_sensitive_binary_argument1_sensitive:
        value: ${binary1_input_sensitive & binary2_input}
        sensitive: true
    - and_binary_argument2_sensitive: ${binary1_input & binary2_input_sensitive}
    - and_sensitive_binary_argument2_sensitive:
        value: ${binary1_input & binary2_input_sensitive}
        sensitive: true
    - and_binary_arguments_sensitive: ${binary1_input_sensitive & binary2_input_sensitive}
    - and_sensitive_binary_arguments_sensitive:
        value: ${binary1_input_sensitive & binary2_input_sensitive}
        sensitive: true

    - or_binary_arguments: ${binary1_input | binary2_input}
    - or_sensitive_binary_arguments:
        value: ${binary1_input | binary2_input}
        sensitive: true
    - or_binary_argument1_sensitive: ${binary1_input_sensitive | binary2_input}
    - or_sensitive_binary_argument1_sensitive:
        value: ${binary1_input_sensitive | binary2_input}
        sensitive: true
    - or_binary_argument2_sensitive: ${binary1_input | binary2_input_sensitive}
    - or_sensitive_binary_argument2_sensitive:
        value: ${binary1_input | binary2_input_sensitive}
        sensitive: true
    - or_binary_arguments_sensitive: ${binary1_input_sensitive | binary2_input_sensitive}
    - or_sensitive_binary_arguments_sensitive:
        value: ${binary1_input_sensitive | binary2_input_sensitive}
        sensitive: true

    - xor_binary_arguments: ${binary1_input ^ binary2_input}
    - xor_sensitivebinary_arguments:
        value: ${binary1_input ^ binary2_input}
        sensitive: true
    - xor_binary_argument1_sensitive: ${binary1_input_sensitive ^ binary2_input}
    - xor_sensitive_binary_argument1_sensitive:
        value: ${binary1_input_sensitive ^ binary2_input}
        sensitive: true
    - xor_binary_argument2_sensitive: ${binary1_input ^ binary2_input_sensitive}
    - xor_sensitive_binary_argument2_sensitive:
        value: ${binary1_input ^ binary2_input_sensitive}
        sensitive: true
    - xor_binary_arguments_sensitive: ${binary1_input_sensitive ^ binary2_input_sensitive}
    - xor_sensitive_binary_arguments_sensitive:
        value: ${binary1_input_sensitive ^ binary2_input_sensitive}
        sensitive: true

    - not_binary_argument1: ${~binary1_input}
    - not_sensitive_binary_argument1:
        value: ${~binary1_input}
        sensitive: true
    - not_binary_argument2: ${~binary2_input}
    - not_sensitive_binary_argument2:
        value: ${~binary2_input}
        sensitive: true
    - not_binary_argument1_sensitive: ${~binary1_input_sensitive}
    - not_sensitive_binary_argument1_sensitive:
        value: ${~binary1_input_sensitive}
        sensitive: true
    - not_binary_argument2_sensitive: ${~binary2_input_sensitive}
    - not_sensitive_binary_argument2_sensitive:
        value: ${~binary2_input_sensitive}
        sensitive: true

    - lshift_binary_argument1: ${binary1_input << 4}
    - lshift_sensitive_binary_argument1:
        value: ${binary1_input << 4}
        sensitive: true
    - lshift_binary_argument2: ${binary2_input << 4}
    - lshift_sensitive_binary_argument2:
        value: ${binary2_input << 4}
        sensitive: true
    - lshift_binary_argument1_sensitive: ${binary1_input_sensitive << 4}
    - lshift_sensitive_binary_argument1_sensitive:
        value: ${binary1_input_sensitive << 4}
        sensitive: true
    - lshift_binary_argument2_sensitive: ${binary2_input_sensitive << 4}
    - lshift_sensitive_binary_argument2_sensitive:
        value: ${binary2_input_sensitive << 4}
        sensitive: true

    - rshift_binary_argument1: ${binary1_input >> 16}
    - rshift_sensitive_binary_argument1:
        value: ${binary1_input >> 16}
        sensitive: true
    - rshift_binary_argument2: ${binary2_input >> 16}
    - rshift_sensitive_binary_argument2:
        value: ${binary2_input >> 16}
        sensitive: true
    - rshift_binary_argument1_sensitive: ${binary1_input_sensitive >> 16}
    - rshift_sensitive_binary_argument1_sensitive:
        value: ${binary1_input_sensitive >> 16}
        sensitive: true
    - rshift_binary_argument2_sensitive: ${binary2_input_sensitive >> 16}
    - rshift_sensitive_binary_argument2_sensitive:
        value: ${binary2_input_sensitive >> 16}
        sensitive: true

    - hex_binary_argument1: ${hex(binary1_input)}
    - hex_sensitive_binary_argument1:
        value: ${hex(binary1_input)}
        sensitive: true
    - hex_binary_argument2: ${hex(binary2_input)}
    - hex_sensitive_binary_argument2:
        value: ${hex(binary2_input)}
        sensitive: true
    - hex_binary_argument1_sensitive: ${hex(binary1_input_sensitive)}
    - hex_sensitive_binary_argument1_sensitive:
        value: ${hex(binary1_input_sensitive)}
        sensitive: true
    - hex_binary_argument2_sensitive: ${hex(binary2_input_sensitive)}
    - hex_sensitive_binary_argument2_sensitive:
        value: ${hex(binary2_input_sensitive)}
        sensitive: true

    - oct_binary_argument1: ${oct(binary1_input)}
    - oct_sensitive_binary_argument1:
        value: ${oct(binary1_input)}
        sensitive: true
    - oct_binary_argument2: ${oct(binary2_input)}
    - oct_sensitive_binary_argument2:
        value: ${oct(binary2_input)}
        sensitive: true
    - oct_binary_argument1_sensitive: ${oct(binary1_input_sensitive)}
    - oct_sensitive_binary_argument1_sensitive:
        value: ${oct(binary1_input_sensitive)}
        sensitive: true
    - oct_binary_argument2_sensitive: ${oct(binary2_input_sensitive)}
    - oct_sensitive_binary_argument2_sensitive:
        value: ${oct(binary2_input_sensitive)}
        sensitive: true

    - int_float_argument1: ${int(float1_input)}
    - int_sensitive_float_argument1:
        value: ${int(float1_input)}
        sensitive: true
    - int_float_argument2: ${int(float2_input)}
    - int_sensitive_float_argument2:
        value: ${int(float2_input)}
        sensitive: true
    - int_float_argument1_sensitive: ${int(float1_input_sensitive)}
    - int_sensitive_float_argument1_sensitive:
        value:   ${int(float1_input_sensitive)}
        sensitive: true
    - int_float_argument2_sensitive: ${int(float2_input_sensitive)}
    - int_sensitive_float_argument2_sensitive:
        value:   ${int(float2_input_sensitive)}
        sensitive: true

    - long_float_argument1: ${long(float1_input)}
    - long_sensitive_float_argument1:
        value: ${long(float1_input)}
        sensitive: true
    - long_float_argument2: ${long(float2_input)}
    - long_sensitive_float_argument2:
        value: ${long(float2_input)}
        sensitive: true
    - long_float_argument1_sensitive: ${long(float1_input_sensitive)}
    - long_sensitive_float_argument1_sensitive:
        value: ${long(float1_input_sensitive)}
        sensitive: true
    - long_float_argument2_sensitive: ${long(float2_input_sensitive)}
    - long_sensitive_float_argument2_sensitive:
        value: ${long(float2_input_sensitive)}
        sensitive: true

    - float_int_argument1: ${float(int1_input)}
    - float__sensitiveint_argument1:
        value: ${float(int1_input)}
        sensitive: true
    - float_int_argument2: ${float(int2_input)}
    - float_sensitive_int_argument2:
        value: ${float(int2_input)}
        sensitive: true
    - float_int_argument1_sensitive: ${float(int1_input_sensitive)}
    - float_sensitive_int_argument1_sensitive:
        value: ${float(int1_input_sensitive)}
        sensitive: true
    - float_int_argument2_sensitive: ${float(int2_input_sensitive)}
    - float_sensitive_int_argument2_sensitive:
        value: ${float(int2_input_sensitive)}
        sensitive: true

    - chr_int_argument1: ${chr(int1_input)}
    - chr_sensitive_int_argument1:
        value: ${chr(int1_input)}
        sensitive: true
    - chr_int_argument2: ${chr(int2_input)}
    - chr_sensitive_int_argument2:
        value: ${chr(int2_input)}
        sensitive: true
    - chr_int_argument1_sensitive: ${chr(int1_input_sensitive)}
    - chr_sensitive_int_argument1_sensitive:
        value: ${chr(int1_input_sensitive)}
        sensitive: true
    - chr_int_argument2_sensitive: ${chr(int2_input_sensitive)}
    - chr_sensitive_int_argument2_sensitive:
        value: ${chr(int2_input_sensitive)}
        sensitive: true

    - unichr_int_argument1: ${unichr(int1_input)}
    - unichr_sensitive_int_argument1:
        value: ${unichr(int1_input)}
        sensitive: true
    - unichr_int_argument2: ${unichr(int2_input)}
    - unichr_sensitive_int_argument2:
        value: ${unichr(int2_input)}
        sensitive: true
    - unichr_int_argument1_sensitive: ${unichr(int1_input_sensitive)}
    - unichr_sensitive_int_argument1_sensitive:
        value: ${unichr(int1_input_sensitive)}
        sensitive: true
    - unichr_int_argument2_sensitive: ${unichr(int2_input_sensitive)}
    - unichr_sensitive_int_argument2_sensitive:
        value: ${unichr(int2_input_sensitive)}
        sensitive: true

    - bool_int_argument1: ${bool(int1_input)}
    - bool_sensitive_int_argument1:
        value: ${bool(int1_input)}
        sensitive: true
    - bool_int_argument2: ${bool(int2_input)}
    - bool_sensitive_int_argument2:
        value: ${bool(int2_input)}
        sensitive: true
    - bool_int_argument1_sensitive: ${bool(int1_input_sensitive)}
    - bool_sensitive_int_argument1_sensitive:
        value: ${bool(int1_input_sensitive)}
        sensitive: true
    - bool_int_argument2_sensitive: ${bool(int2_input_sensitive)}
    - bool_sensitive_int_argument2_sensitive:
        value: ${bool(int2_input_sensitive)}
        sensitive: true

    - str_int_argument1: ${str(int1_input)}
    - str_sensitive_int_argument1:
        sensitive: true
        value: ${str(int1_input)}
    - str_int_argument2: ${str(int2_input)}
    - str_sensitive_int_argument2:
        sensitive: true
        value: ${str(int2_input)}
    - str_int_argument1_sensitive: ${str(int1_input_sensitive)}
    - str_sensitive_int_argument1_sensitive:
        sensitive: true
        value: ${str(int1_input_sensitive)}
    - str_int_argument2_sensitive: ${str(int2_input_sensitive)}
    - str_sensitive_int_argument2_sensitive:
        sensitive: true
        value: ${str(int2_input_sensitive)}

    - str_float_argument1: ${str(float1_input)}
    - str_sensitive_float_argument1:
        value: ${str(float1_input)}
        sensitive: true
    - str_float_argument2: ${str(float2_input)}
    - str_sensitive_float_argument2:
        value: ${str(float2_input)}
        sensitive: true
    - str_float_argument1_sensitive: ${str(float1_input_sensitive)}
    - str_sensitive_float_argument1_sensitive:
        value: ${str(float1_input_sensitive)}
        sensitive: true
    - str_float_argument2_sensitive: ${str(float2_input_sensitive)}
    - str_sensitive_float_argument2_sensitive:
        value: ${str(float2_input_sensitive)}
        sensitive: true

    - unicode_int_argument1: ${unicode(int1_input)}
    - unicode_sensitive_int_argument1:
        value: ${unicode(int1_input)}
        sensitive: true
    - unicode_int_argument2: ${unicode(int2_input)}
    - unicode_sensitive_int_argument2:
        value: ${unicode(int2_input)}
        sensitive: true
    - unicode_int_argument1_sensitive: ${unicode(int1_input_sensitive)}
    - unicode_sensitive_int_argument1_sensitive:
        value: ${unicode(int1_input_sensitive)}
        sensitive: true
    - unicode_int_argument2_sensitive: ${unicode(int2_input_sensitive)}
    - unicode_sensitive_int_argument2_sensitive:
        value: ${unicode(int2_input_sensitive)}
        sensitive: true

    - unicode_float_argument1: ${unicode(float1_input)}
    - unicode_sensitive_float_argument1:
        value: ${unicode(float1_input)}
        sensitive: true
    - unicode_float_argument2: ${unicode(float2_input)}
    - unicode_sensitive_float_argument2:
        value: ${unicode(float2_input)}
        sensitive: true
    - unicode_float_argument1_sensitive: ${unicode(float1_input_sensitive)}
    - unicode_sensitive_float_argument1_sensitive:
        value: ${unicode(float1_input_sensitive)}
        sensitive: true
    - unicode_float_argument2_sensitive: ${unicode(float2_input_sensitive)}
    - unicode_sensitive_float_argument2_sensitive:
        value: ${unicode(float2_input_sensitive)}
        sensitive: true

    - unicode_str_argument1: ${unicode(str1_input)}
    - unicode_sensitive_str_argument1:
        value: ${unicode(str1_input)}
        sensitive: true
    - unicode_str_argument2: ${unicode(str2_input)}
    - unicode_sensitive_str_argument2:
        value: ${unicode(str2_input)}
        sensitive: true
    - unicode_str_argument1_sensitive: ${unicode(str1_input_sensitive)}
    - unicode_sensitive_str_argument1_sensitive:
        value: ${unicode(str1_input_sensitive)}
        sensitive: true
    - unicode_str_argument2_sensitive: ${unicode(str2_input_sensitive)}
    - unicode_sensitive_str_argument2_sensitive:
        value: ${unicode(str2_input_sensitive)}
        sensitive: true

    - trunc_float_argument1: ${int(float1_input)}
    - trunc_sensitive_float_argument1:
        value: ${int(float1_input)}
        sensitive: true
    - trunc_float_argument2: ${int(float2_input)}
    - trunc_sensitive_float_argument2:
        value: ${int(float2_input)}
        sensitive: true
    - trunc_float_argument1_sensitive: ${int(float1_input_sensitive)}
    - trunc_sensitive_float_argument1_sensitive:
        value: ${int(float1_input_sensitive)}
        sensitive: true
    - trunc_float_argument2_sensitive: ${int(float2_input_sensitive)}
    - trunc_sensitive_float_argument2_sensitive:
        value: ${int(float2_input_sensitive)}
        sensitive: true

    - range_int_argument1: ${range(int1_input)}
    - range_sensitive_int_argument1:
        value: ${range(int1_input)}
        sensitive: true
    - range_int_argument2: ${range(int2_input)}
    - range_sensitive_int_argument2:
        value: ${range(int2_input)}
        sensitive: true
    - range_int_argument1_sensitive: ${range(int1_input_sensitive)}
    - range_sensitive_int_argument1_sensitive:
        value: ${range(int1_input_sensitive)}
        sensitive: true
    - range_int_argument2_sensitive: ${range(int2_input_sensitive)}
    - range_sensitive_int_argument2_sensitive:
        value: ${range(int2_input_sensitive)}
        sensitive: true

    - pos_int_argument1: ${+int1_input}
    - pos_sensitive_int_argument1:
        value: ${+int1_input}
        sensitive: true
    - pos_int_argument2: ${+int2_input}
    - pos_sensitive_int_argument2:
        value: ${+int2_input}
        sensitive: true
    - pos_int_argument1_sensitive: ${+int1_input_sensitive}
    - pos_sensitive_int_argument1_sensitive:
        value: ${+int1_input_sensitive}
        sensitive: true
    - pos_int_argument2_sensitive: ${+int2_input_sensitive}
    - pos_sensitive_int_argument2_sensitive:
        value: ${+int2_input_sensitive}
        sensitive: true

    - pos_float_argument1: ${+float1_input}
    - pos_sensitive_float_argument1:
        value: ${+float1_input}
        sensitive: true
    - pos_float_argument2: ${+float2_input}
    - pos_sensitive_float_argument2:
        value: ${+float2_input}
        sensitive: true
    - pos_float_argument1_sensitive: ${+float1_input_sensitive}
    - pos_sensitive_float_argument1_sensitive:
        value: ${+float1_input_sensitive}
        sensitive: true
    - pos_float_argument2_sensitive: ${+float2_input_sensitive}
    - pos_sensitive_float_argument2_sensitive:
        value: ${+float2_input_sensitive}
        sensitive: true

    - neg_int_argument1: ${-int1_input}
    - neg_sensitive_int_argument1:
        value: ${-int1_input}
        sensitive: true
    - neg_int_argument2: ${-int2_input}
    - neg_sensitive_int_argument2:
        value: ${-int2_input}
        sensitive: true
    - neg_int_argument1_sensitive: ${-int1_input_sensitive}
    - neg_sensitive_int_argument1_sensitive:
        value: ${-int1_input_sensitive}
        sensitive: true
    - neg_int_argument2_sensitive: ${-int2_input_sensitive}
    - neg_sensitive_int_argument2_sensitive:
        value: ${-int2_input_sensitive}
        sensitive: true

    - neg_float_argument1: ${-float1_input}
    - neg_sensitive_float_argument1:
        value: ${-float1_input}
        sensitive: true
    - neg_float_argument2: ${-float2_input}
    - neg_sensitive_float_argument2:
        value: ${-float2_input}
        sensitive: true
    - neg_float_argument1_sensitive: ${-float1_input_sensitive}
    - neg_sensitive_float_argument1_sensitive:
        value: ${-float1_input_sensitive}
        sensitive: true
    - neg_float_argument2_sensitive: ${-float2_input_sensitive}
    - neg_sensitive_float_argument2_sensitive:
        value: ${-float2_input_sensitive}
        sensitive: true

    - abs_int_argument1: ${abs(int1_input)}
    - abs_sensitive_int_argument1:
        value: ${abs(int1_input)}
        sensitive: true
    - abs_int_argument2: ${abs(int2_input)}
    - abs_sensitive_int_argument2:
        value: ${abs(int2_input)}
        sensitive: true
    - abs_int_argument1_sensitive: ${abs(int1_input_sensitive)}
    - abs_sensitive_int_argument1_sensitive:
        value: ${abs(int1_input_sensitive)}
        sensitive: true
    - abs_int_argument2_sensitive: ${abs(int2_input_sensitive)}
    - abs_sensitive_int_argument2_sensitive:
        value: ${abs(int2_input_sensitive)}
        sensitive: true

    - abs_float_argument1: ${abs(float1_input)}
    - abs_sensitive_float_argument1:
        value: ${abs(float1_input)}
        sensitive: true
    - abs_float_argument2: ${abs(float2_input)}
    - abs_sensitive_float_argument2:
        value: ${abs(float2_input)}
        sensitive: true
    - abs_float_argument1_sensitive: ${abs(float1_input_sensitive)}
    - abs_sensitive_float_argument1_sensitive:
        value: ${abs(float1_input_sensitive)}
        sensitive: true
    - abs_float_argument2_sensitive: ${abs(float2_input_sensitive)}
    - abs_sensitive_float_argument2_sensitive:
        value: ${abs(float2_input_sensitive)}
        sensitive: true

    - round_int_argument1: ${round(int1_input)}
    - round_sensitive_int_argument1:
        value: ${round(int1_input)}
        sensitive: true
    - round_int_argument2: ${round(int2_input)}
    - round_sensitive_int_argument2:
        value: ${round(int2_input)}
        sensitive: true
    - round_int_argument1_sensitive: ${round(int1_input_sensitive)}
    - round_sensitive_int_argument1_sensitive:
        value: ${round(int1_input_sensitive)}
        sensitive: true
    - round_int_argument2_sensitive: ${round(int2_input_sensitive)}
    - round_sensitive_int_argument2_sensitive:
        value: ${round(int2_input_sensitive)}
        sensitive: true

    - round_float_argument1: ${round(float1_input)}
    - round_sensitive_float_argument1:
        value: ${round(float1_input)}
        sensitive: true
    - round_float_argument2: ${round(float2_input)}
    - round_sensitive_float_argument2:
        value: ${round(float2_input)}
        sensitive: true
    - round_float_argument1_sensitive: ${round(float1_input_sensitive)}
    - round_sensitive_float_argument1_sensitive:
        value: ${round(float1_input_sensitive)}
        sensitive: true
    - round_float_argument2_sensitive: ${round(float2_input_sensitive)}
    - round_sensitive_float_argument2_sensitive:
        value: ${round(float2_input_sensitive)}
        sensitive: true

    - pow1_int_argument1: ${pow(int1_input, 2)}
    - pow1_sensitive_int_argument1:
        value: ${pow(int1_input, 2)}
        sensitive: true
    - pow1_int_argument2: ${pow(int2_input, 2)}
    - pow1_sensitive_int_argument2:
        value: ${pow(int2_input, 2)}
        sensitive: true
    - pow1_int_argument1_sensitive: ${pow(int1_input_sensitive, 2)}
    - pow1_sensitive_int_argument1_sensitive:
        value: ${pow(int1_input_sensitive, 2)}
        sensitive: true
    - pow1_int_argument2_sensitive: ${pow(int2_input_sensitive, 2)}
    - pow1_sensitive_int_argument2_sensitive:
        value: ${pow(int2_input_sensitive, 2)}
        sensitive: true

    - pow1_float_argument1: ${pow(float1_input, 2)}
    - pow1_sensitive_float_argument1:
        value: ${pow(float1_input, 2)}
        sensitive: true
    - pow1_float_argument2: ${pow(float2_input, 2)}
    - pow1_sensitive_float_argument2:
        value: ${pow(float2_input, 2)}
        sensitive: true
    - pow1_float_argument1_sensitive: ${pow(float1_input_sensitive, 2)}
    - pow1_sensitive_float_argument1_sensitive:
        value: ${pow(float1_input_sensitive, 2)}
        sensitive: true
    - pow1_float_argument2_sensitive: ${pow(float2_input_sensitive, 2)}
    - pow1_sensitive_float_argument2_sensitive:
        value: ${pow(float2_input_sensitive, 2)}
        sensitive: true

    - pow2_int_argument1: ${int1_input ** 2}
    - pow2_sensitive_int_argument1:
        value: ${int1_input ** 2}
        sensitive: true
    - pow2_int_argument2: ${int2_input ** 2}
    - pow2_sensitive_int_argument2:
        value: ${int2_input ** 2}
        sensitive: true
    - pow2_int_argument1_sensitive: ${int1_input_sensitive ** 2}
    - pow2_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive ** 2}
        sensitive: true
    - pow2_int_argument2_sensitive: ${int2_input_sensitive ** 2}
    - pow2_sensitive_int_argument2_sensitive:
        value: ${int2_input_sensitive ** 2}
        sensitive: true

    - pow2_float_argument1: ${float1_input ** 2}
    - pow2_sensitive_float_argument1:
        value: ${float1_input ** 2}
        sensitive: true
    - pow2_float_argument2: ${float2_input ** 2}
    - pow2_sensitive_float_argument2:
        value: ${float2_input ** 2}
        sensitive: true
    - pow2_float_argument1_sensitive: ${float1_input_sensitive ** 2}
    - pow2_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive ** 2}
        sensitive: true
    - pow2_float_argument2_sensitive: ${float2_input_sensitive ** 2}
    - pow2_sensitive_float_argument2_sensitive:
        value: ${float2_input_sensitive ** 2}
        sensitive: true

    - max_int_arguments: ${max(int1_input, int2_input)}
    - max_sensitive_int_arguments:
        value: ${max(int1_input, int2_input)}
        sensitive: true
    - max_int_argument1_sensitive: ${max(int1_input_sensitive, int2_input)}
    - max_sensitive_int_argument1_sensitive:
        value: ${max(int1_input_sensitive, int2_input)}
        sensitive: true
    - max_int_argument2_sensitive: ${max(int1_input, int2_input_sensitive)}
    - max_sensitive_int_argument2_sensitive:
        value: ${max(int1_input, int2_input_sensitive)}
        sensitive: true
    - max_int_arguments_sensitive: ${max(int1_input_sensitive, int2_input_sensitive)}
    - max_sensitive_int_arguments_sensitive:
        value: ${max(int1_input_sensitive, int2_input_sensitive)}
        sensitive: true

    - max_float_arguments: ${max(float1_input, float2_input)}
    - max_sensitive_float_arguments:
        value: ${max(float1_input, float2_input)}
        sensitive: true
    - max_float_argument1_sensitive: ${max(float1_input_sensitive, float2_input)}
    - max_sensitive_float_argument1_sensitive:
        value: ${max(float1_input_sensitive, float2_input)}
        sensitive: true
    - max_float_argument2_sensitive: ${max(float1_input, float2_input_sensitive)}
    - max_sensitive_float_argument2_sensitive:
        value: ${max(float1_input, float2_input_sensitive)}
        sensitive: true
    - max_float_arguments_sensitive: ${max(float1_input_sensitive, float2_input_sensitive)}
    - max_sensitive_float_arguments_sensitive:
        value: ${max(float1_input_sensitive, float2_input_sensitive)}
        sensitive: true

    - max_str_arguments: ${max(str1_input, str2_input)}
    - max_sensitive_str_arguments:
        value: ${max(str1_input, str2_input)}
        sensitive: true
    - max_str_argument1_sensitive: ${max(str1_input_sensitive, str2_input)}
    - max_sensitive_str_argument1_sensitive:
        value: ${max(str1_input_sensitive, str2_input)}
        sensitive: true
    - max_str_argument2_sensitive: ${max(str1_input, str2_input_sensitive)}
    - max_sensitive_str_argument2_sensitive:
        value: ${max(str1_input, str2_input_sensitive)}
        sensitive: true
    - max_str_arguments_sensitive: ${max(str1_input_sensitive, str2_input_sensitive)}
    - max_sensitive_str_arguments_sensitive:
        value: ${max(str1_input_sensitive, str2_input_sensitive)}
        sensitive: true

    - min_int_arguments: ${min(int1_input, int2_input)}
    - min_sensitive_int_arguments:
        value: ${min(int1_input, int2_input)}
        sensitive: true
    - min_int_argument1_sensitive: ${min(int1_input_sensitive, int2_input)}
    - min_sensitive_int_argument1_sensitive:
        value: ${min(int1_input_sensitive, int2_input)}
        sensitive: true
    - min_int_argument2_sensitive: ${min(int1_input, int2_input_sensitive)}
    - min_sensitive_int_argument2_sensitive:
        value: ${min(int1_input, int2_input_sensitive)}
        sensitive: true
    - min_int_arguments_sensitive: ${min(int1_input_sensitive, int2_input_sensitive)}
    - min_sensitive_int_arguments_sensitive:
        value: ${min(int1_input_sensitive, int2_input_sensitive)}
        sensitive: true

    - min_float_arguments: ${min(float1_input, float2_input)}
    - min_sensitive_float_arguments:
        value: ${min(float1_input, float2_input)}
        sensitive: true
    - min_float_argument1_sensitive: ${min(float1_input_sensitive, float2_input)}
    - min_sensitive_float_argument1_sensitive:
        value: ${min(float1_input_sensitive, float2_input)}
        sensitive: true
    - min_float_argument2_sensitive: ${min(float1_input, float2_input_sensitive)}
    - min_sensitive_float_argument2_sensitive:
        value: ${min(float1_input, float2_input_sensitive)}
        sensitive: true
    - min_float_arguments_sensitive: ${min(float1_input_sensitive, float2_input_sensitive)}
    - min_sensitive_float_arguments_sensitive:
        value: ${min(float1_input_sensitive, float2_input_sensitive)}
        sensitive: true

    - min_str_arguments: ${min(str1_input, str2_input)}
    - min_sensitive_str_arguments:
        value: ${min(str1_input, str2_input)}
        sensitive: true
    - min_str_argument1_sensitive: ${min(str1_input_sensitive, str2_input)}
    - min_sensitive_str_argument1_sensitive:
        value: ${min(str1_input_sensitive, str2_input)}
        sensitive: true
    - min_str_argument2_sensitive: ${min(str1_input, str2_input_sensitive)}
    - min_sensitive_str_argument2_sensitive:
        value: ${min(str1_input, str2_input_sensitive)}
        sensitive: true
    - min_str_arguments_sensitive: ${min(str1_input_sensitive, str2_input_sensitive)}
    - min_sensitive_str_arguments_sensitive:
        value: ${min(str1_input_sensitive, str2_input_sensitive)}
        sensitive: true

    - add1_int_arguments: ${int1_input + int2_input}
    - add1_sensitive_int_arguments:
        value: ${int1_input + int2_input}
        sensitive: true
    - add1_int_argument1_sensitive: ${int1_input_sensitive + int2_input}
    - add1_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive + int2_input}
        sensitive: true
    - add1_int_argument2_sensitive: ${int1_input + int2_input_sensitive}
    - add1_sensitive_int_argument2_sensitive:
        value: ${int1_input + int2_input_sensitive}
        sensitive: true
    - add1_int_arguments_sensitive: ${int1_input_sensitive + int2_input_sensitive}
    - add1_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive + int2_input_sensitive}
        sensitive: true

    - add1_str_arguments: ${str1_input + str2_input}
    - add1_sensitive_str_arguments:
        value: ${str1_input + str2_input}
        sensitive: true
    - add1_str_argument1_sensitive: ${str1_input_sensitive + str2_input}
    - add1_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive + str2_input}
        sensitive: true
    - add1_str_argument2_sensitive: ${str1_input + str2_input_sensitive}
    - add1_sensitive_str_argument2_sensitive:
        value: ${str1_input + str2_input_sensitive}
        sensitive: true
    - add1_str_arguments_sensitive: ${str1_input_sensitive + str2_input_sensitive}
    - add1_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive + str2_input_sensitive}
        sensitive: true

    - add1_float_arguments: ${float1_input + float2_input}
    - add1_sensitive_float_arguments:
        value: ${float1_input + float2_input}
        sensitive: true
    - add1_float_argument1_sensitive: ${float1_input_sensitive + float2_input}
    - add1_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive + float2_input}
        sensitive: true
    - add1_float_argument2_sensitive: ${float1_input + float2_input_sensitive}
    - add1_sensitive_float_argument2_sensitive:
        value: ${float1_input + float2_input_sensitive}
        sensitive: true
    - add1_float_arguments_sensitive: ${float1_input_sensitive + float2_input_sensitive}
    - add1_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive + float2_input_sensitive}
        sensitive: true

    - add2_int_arguments: ${int1_input + 5 + int2_input}
    - add2_sensitive_int_arguments:
        value: ${int1_input + 5 + int2_input}
        sensitive: true
    - add2_int_argument1_sensitive: ${int1_input_sensitive + 5 + int2_input}
    - add2_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive + 5 + int2_input}
        sensitive: true
    - add2_int_argument2_sensitive: ${int1_input + 5 + int2_input_sensitive}
    - add2_sensitive_int_argument2_sensitive:
        value: ${int1_input + 5 + int2_input_sensitive}
        sensitive: true
    - add2_int_arguments_sensitive: ${int1_input_sensitive + 5 + int2_input_sensitive}
    - add2_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive + 5 + int2_input_sensitive}
        sensitive: true

    - add2_str_arguments: ${str1_input + 'c' + str2_input}
    - add2_sensitive_str_arguments:
        value: ${str1_input + 'c' + str2_input}
        sensitive: true
    - add2_str_argument1_sensitive: ${str1_input_sensitive + 'c' + str2_input}
    - add2_sensitive_str_argument1_sensitive:
        value: ${str1_input_sensitive + 'c' + str2_input}
        sensitive: true
    - add2_str_argument2_sensitive: ${str1_input + 'c' + str2_input_sensitive}
    - add2_sensitive_str_argument2_sensitive:
        value: ${str1_input + 'c' + str2_input_sensitive}
        sensitive: true
    - add2_str_arguments_sensitive: ${str1_input_sensitive + 'c' + str2_input_sensitive}
    - add2_sensitive_str_arguments_sensitive:
        value: ${str1_input_sensitive + 'c' + str2_input_sensitive}
        sensitive: true

    - add2_float_arguments: ${float1_input + 7.0 + float2_input}
    - add2_sensitive_float_arguments:
        value: ${float1_input + 7.0 + float2_input}
        sensitive: true
    - add2_float_argument1_sensitive: ${float1_input_sensitive + 7.0 + float2_input}
    - add2_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive + 7.0 + float2_input}
        sensitive: true
    - add2_float_argument2_sensitive: ${float1_input + 7.0 + float2_input_sensitive}
    - add2_sensitive_float_argument2_sensitive:
        value: ${float1_input + 7.0 + float2_input_sensitive}
        sensitive: true
    - add2_float_arguments_sensitive: ${float1_input_sensitive + 7.0 + float2_input_sensitive}
    - add2_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive + 7.0 + float2_input_sensitive}
        sensitive: true

    - sub_int_arguments: ${int1_input - int2_input}
    - sub_sensitive_int_arguments:
        value: ${int1_input - int2_input}
        sensitive: true
    - sub_int_argument1_sensitive: ${int1_input_sensitive - int2_input}
    - sub_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive - int2_input}
        sensitive: true
    - sub_int_argument2_sensitive: ${int1_input - int2_input_sensitive}
    - sub_sensitive_int_argument2_sensitive:
        value: ${int1_input - int2_input_sensitive}
        sensitive: true
    - sub_int_arguments_sensitive: ${int1_input_sensitive - int2_input_sensitive}
    - sub_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive - int2_input_sensitive}
        sensitive: true

    - sub_float_arguments: ${float1_input - float2_input}
    - sub_sensitive_float_arguments:
        value: ${float1_input - float2_input}
        sensitive: true
    - sub_float_argument1_sensitive: ${float1_input_sensitive - float2_input}
    - sub_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive - float2_input}
        sensitive: true
    - sub_float_argument2_sensitive: ${float1_input - float2_input_sensitive}
    - sub_sensitive_float_argument2_sensitive:
        value: ${float1_input - float2_input_sensitive}
        sensitive: true
    - sub_float_arguments_sensitive: ${float1_input_sensitive - float2_input_sensitive}
    - sub_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive - float2_input_sensitive}
        sensitive: true

    - mul_int_arguments: ${int1_input * int2_input}
    - mul_sensitive_int_arguments:
        value: ${int1_input * int2_input}
        sensitive: true
    - mul_int_argument1_sensitive: ${int1_input_sensitive * int2_input}
    - mul_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive * int2_input}
        sensitive: true
    - mul_int_argument2_sensitive: ${int1_input * int2_input_sensitive}
    - mul_sensitive_int_argument2_sensitive:
        value: ${int1_input * int2_input_sensitive}
        sensitive: true
    - mul_int_arguments_sensitive: ${int1_input_sensitive * int2_input_sensitive}
    - mul_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive * int2_input_sensitive}
        sensitive: true

    - mul_float_arguments: ${float1_input * float2_input}
    - mul_sensitive_float_arguments:
        value: ${float1_input * float2_input}
        sensitive: true
    - mul_float_argument1_sensitive: ${float1_input_sensitive * float2_input}
    - mul_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive * float2_input}
        sensitive: true
    - mul_float_argument2_sensitive: ${float1_input * float2_input_sensitive}
    - mul_sensitive_float_argument2_sensitive:
        value: ${float1_input * float2_input_sensitive}
        sensitive: true
    - mul_float_arguments_sensitive: ${float1_input_sensitive * float2_input_sensitive}
    - mul_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive * float2_input_sensitive}
        sensitive: true

    - div_int_arguments: ${int1_input / int2_input}
    - div_sensitive_int_arguments:
        value: ${int1_input / int2_input}
        sensitive: true
    - div_int_argument1_sensitive: ${int1_input_sensitive / int2_input}
    - div_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive / int2_input}
        sensitive: true
    - div_int_argument2_sensitive: ${int1_input / int2_input_sensitive}
    - div_sensitive_int_argument2_sensitive:
        value: ${int1_input / int2_input_sensitive}
        sensitive: true
    - div_int_arguments_sensitive: ${int1_input_sensitive / int2_input_sensitive}
    - div_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive / int2_input_sensitive}
        sensitive: true

    - div_float_arguments: ${float1_input / float2_input}
    - div_sensitive_float_arguments:
        value: ${float1_input / float2_input}
        sensitive: true
    - div_float_argument1_sensitive: ${float1_input_sensitive / float2_input}
    - div_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive / float2_input}
        sensitive: true
    - div_float_argument2_sensitive: ${float1_input / float2_input_sensitive}
    - div_sensitive_float_argument2_sensitive:
        value: ${float1_input / float2_input_sensitive}
        sensitive: true
    - div_float_arguments_sensitive: ${float1_input_sensitive / float2_input_sensitive}
    - div_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive / float2_input_sensitive}
        sensitive: true

    - truediv_int_arguments: ${int1_input / int2_input}
    - truediv_sensitive_int_arguments:
        value: ${int1_input / int2_input}
        sensitive: true
    - truediv_int_argument1_sensitive: ${int1_input_sensitive / int2_input}
    - truediv_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive / int2_input}
        sensitive: true
    - truediv_int_argument2_sensitive: ${int1_input / int2_input_sensitive}
    - truediv_sensitive_int_argument2_sensitive:
        value: ${int1_input / int2_input_sensitive}
        sensitive: true
    - truediv_int_arguments_sensitive: ${int1_input_sensitive / int2_input_sensitive}
    - truediv_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive / int2_input_sensitive}
        sensitive: true

    - truediv_float_arguments: ${float1_input / float2_input}
    - truediv_sensitive_float_arguments:
        value: ${float1_input / float2_input}
        sensitive: true
    - truediv_float_argument1_sensitive: ${float1_input_sensitive / float2_input}
    - truediv_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive / float2_input}
        sensitive: true
    - truediv_float_argument2_sensitive: ${float1_input / float2_input_sensitive}
    - truediv_sensitive_float_argument2_sensitive:
        value: ${float1_input / float2_input_sensitive}
        sensitive: true
    - truediv_float_arguments_sensitive: ${float1_input_sensitive / float2_input_sensitive}
    - truediv_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive / float2_input_sensitive}
        sensitive: true

    - floordiv_int_arguments: ${int1_input // int2_input}
    - floordiv_sensitive_int_arguments:
        value: ${int1_input // int2_input}
        sensitive: true
    - floordiv_int_argument1_sensitive: ${int1_input_sensitive // int2_input}
    - floordiv_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive // int2_input}
        sensitive: true
    - floordiv_int_argument2_sensitive: ${int1_input // int2_input_sensitive}
    - floordiv_sensitive_int_argument2_sensitive:
        value: ${int1_input // int2_input_sensitive}
        sensitive: true
    - floordiv_int_arguments_sensitive: ${int1_input_sensitive // int2_input_sensitive}
    - floordiv_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive // int2_input_sensitive}
        sensitive: true

    - floordiv_float_arguments: ${float1_input // float2_input}
    - floordiv_sensitive_float_arguments:
        value: ${float1_input // float2_input}
        sensitive: true
    - floordiv_float_argument1_sensitive: ${float1_input_sensitive // float2_input}
    - floordiv_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive // float2_input}
        sensitive: true
    - floordiv_float_argument2_sensitive: ${float1_input // float2_input_sensitive}
    - floordiv_sensitive_float_argument2_sensitive:
        value: ${float1_input // float2_input_sensitive}
        sensitive: true
    - floordiv_float_arguments_sensitive: ${float1_input_sensitive // float2_input_sensitive}
    - floordiv_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive // float2_input_sensitive}
        sensitive: true

    - mod_int_arguments: ${int1_input % int2_input}
    - mod_sensitive_int_arguments:
        value: ${int1_input % int2_input}
        sensitive: true
    - mod_int_argument1_sensitive: ${int1_input_sensitive % int2_input}
    - mod_sensitive_int_argument1_sensitive:
        value: ${int1_input_sensitive % int2_input}
        sensitive: true
    - mod_int_argument2_sensitive: ${int1_input % int2_input_sensitive}
    - mod_sensitive_int_argument2_sensitive:
        value: ${int1_input % int2_input_sensitive}
        sensitive: true
    - mod_int_arguments_sensitive: ${int1_input_sensitive % int2_input_sensitive}
    - mod_sensitive_int_arguments_sensitive:
        value: ${int1_input_sensitive % int2_input_sensitive}
        sensitive: true

    - mod_float_arguments: ${float1_input % float2_input}
    - mod_sensitive_float_arguments:
        value: ${float1_input % float2_input}
        sensitive: true
    - mod_float_argument1_sensitive: ${float1_input_sensitive % float2_input}
    - mod_sensitive_float_argument1_sensitive:
        value: ${float1_input_sensitive % float2_input}
        sensitive: true
    - mod_float_argument2_sensitive: ${float1_input % float2_input_sensitive}
    - mod_sensitive_float_argument2_sensitive:
        value: ${float1_input % float2_input_sensitive}
        sensitive: true
    - mod_float_arguments_sensitive: ${float1_input_sensitive % float2_input_sensitive}
    - mod_sensitive_float_arguments_sensitive:
        value: ${float1_input_sensitive % float2_input_sensitive}
        sensitive: true

    - divmod_int_argument1: ${divmod(int1_input, 2)}
    - divmod_sensitive_int_argument1:
        value: ${divmod(int1_input, 2)}
        sensitive: true
    - divmod_int_argument2: ${divmod(int2_input, 2)}
    - divmod_sensitive_int_argument2:
        value: ${divmod(int2_input, 2)}
        sensitive: true
    - divmod_int_argument1_sensitive: ${divmod(int1_input_sensitive, 2)}
    - divmod_sensitive_int_argument1_sensitive:
        value: ${divmod(int1_input_sensitive, 2)}
        sensitive: true
    - divmod_int_argument2_sensitive: ${divmod(int2_input_sensitive, 2)}
    - divmod_sensitive_int_argument2_sensitive:
        value: ${divmod(int2_input_sensitive, 2)}
        sensitive: true

    - divmod_float_argument1: ${divmod(float1_input, 2)}
    - divmod_sensitive_float_argument1:
        value: ${divmod(float1_input, 2)}
        sensitive: true
    - divmod_float_argument2: ${divmod(float2_input, 2)}
    - divmod_sensitive_float_argument2:
        value: ${divmod(float2_input, 2)}
        sensitive: true
    - divmod_float_argument1_sensitive: ${divmod(float1_input_sensitive, 2)}
    - divmod_sensitive_float_argument1_sensitive:
        value: ${divmod(float1_input_sensitive, 2)}
        sensitive: true
    - divmod_float_argument2_sensitive: ${divmod(float2_input_sensitive, 2)}
    - divmod_sensitive_float_argument2_sensitive:
        value: ${divmod(float2_input_sensitive, 2)}
        sensitive: true

    - repr_int_argument1: ${repr(int1_input)}
    - repr_sensitive_int_argument1:
        value: ${repr(int1_input)}
        sensitive: true
    - repr_int_argument2: ${repr(int2_input)}
    - repr_sensitive_int_argument2:
        value: ${repr(int2_input)}
        sensitive: true
    - repr_int_argument1_sensitive: ${repr(int1_input_sensitive)}
    - repr_sensitive_int_argument1_sensitive:
        value: ${repr(int1_input_sensitive)}
        sensitive: true
    - repr_int_argument2_sensitive: ${repr(int2_input_sensitive)}
    - repr_sensitive_int_argument2_sensitive:
        value: ${repr(int2_input_sensitive)}
        sensitive: true

    - repr_float_argument1: ${repr(float1_input)}
    - repr_sensitive_float_argument1:
        value: ${repr(float1_input)}
        sensitive: true
    - repr_float_argument2: ${repr(float2_input)}
    - repr_sensitive_float_argument2:
        value: ${repr(float2_input)}
        sensitive: true
    - repr_float_argument1_sensitive: ${repr(float1_input_sensitive)}
    - repr_sensitive_float_argument1_sensitive:
        value: ${repr(float1_input_sensitive)}
        sensitive: true
    - repr_float_argument2_sensitive: ${repr(float2_input_sensitive)}
    - repr_sensitive_float_argument2_sensitive:
        value: ${repr(float2_input_sensitive)}
        sensitive: true

    - repr_str_argument1: ${repr(str1_input)}
    - repr_sensitive_str_argument1:
        value: ${repr(str1_input)}
        sensitive: true
    - repr_str_argument2: ${repr(str2_input)}
    - repr_sensitive_str_argument2:
        value: ${repr(str2_input)}
        sensitive: true
    - repr_str_argument1_sensitive: ${repr(str1_input_sensitive)}
    - repr_sensitive_str_argument1_sensitive:
        value: ${repr(str1_input_sensitive)}
        sensitive: true
    - repr_str_argument2_sensitive: ${repr(str2_input_sensitive)}
    - repr_sensitive_str_argument2_sensitive:
        value: ${repr(str2_input_sensitive)}
        sensitive: true

    - index_list_argument1: ${list1_input.index(2)}
    - index_sensitive_list_argument1:
        value: ${list1_input.index(2)}
        sensitive: true
    - index_list_argument2: ${list2_input.index(15)}
    - index_sensitive_list_argument2:
        value: ${list2_input.index(15)}
        sensitive: true
    - index_list_argument1_sensitive: ${list1_input_sensitive.index(2)}
    - index_sensitive_list_argument1_sensitive:
        value: ${list1_input_sensitive.index(2)}
        sensitive: true
    - index_list_argument2_sensitive: ${list2_input_sensitive.index(15)}
    - index_sensitive_list_argument2_sensitive:
        value: ${list2_input_sensitive.index(15)}
        sensitive: true

    - sorted_list_argument1: ${sorted(list1_input)}
    - sorted_sensitive_list_argument1:
        value: ${sorted(list1_input)}
        sensitive: true
    - sorted_list_argument2: ${sorted(list2_input)}
    - sorted_sensitive_list_argument2:
        value: ${sorted(list2_input)}
        sensitive: true
    - sorted_list_argument1_sensitive: ${sorted(list1_input_sensitive)}
    - sorted_sensitive_list_argument1_sensitive:
        value: ${sorted(list1_input_sensitive)}
        sensitive: true
    - sorted_list_argument2_sensitive: ${sorted(list2_input_sensitive)}
    - sorted_sensitive_list_argument2_sensitive:
        value: ${sorted(list2_input_sensitive)}
        sensitive: true

    - sum_list_argument1: ${sum(list1_input, 0)}
    - sum_sensitive_list_argument1:
        value: ${sum(list1_input, 0)}
        sensitive: true
    - sum_list_argument2: ${sum(list2_input, 0)}
    - sum_sensitive_list_argument2:
        value: ${sum(list2_input, 0)}
        sensitive: true
    - sum_list_argument1_sensitive: ${sum(list1_input_sensitive, 0)}
    - sum_sensitive_list_argument1_sensitive:
        value: ${sum(list1_input_sensitive, 0)}
        sensitive: true
    - sum_list_argument2_sensitive: ${sum(list2_input_sensitive, 0)}
    - sum_sensitive_list_argument2_sensitive:
        value: ${sum(list2_input_sensitive, 0)}
        sensitive: true

    - tuple_list_argument1: ${tuple(list1_input)}
    - tuple_sensitive_list_argument1:
        value: ${tuple(list1_input)}
        sensitive: true
    - tuple_list_argument2: ${tuple(list2_input)}
    - tuple_sensitive_list_argument2:
        value: ${tuple(list2_input)}
        sensitive: true
    - tuple_list_argument1_sensitive: ${tuple(list1_input_sensitive)}
    - tuple_sensitive_list_argument1_sensitive:
        value: ${tuple(list1_input_sensitive)}
        sensitive: true
    - tuple_list_argument2_sensitive: ${tuple(list2_input_sensitive)}
    - tuple_sensitive_list_argument2_sensitive:
        value: ${tuple(list2_input_sensitive)}
        sensitive: true

    - type_int_argument1: ${type(int1_input)}
    - type_sensitive_int_argument1:
        value: ${type(int1_input)}
        sensitive: true
    - type_int_argument2: ${type(int2_input)}
    - type_sensitive_int_argument2:
        value: ${type(int2_input)}
        sensitive: true
    - type_int_argument1_sensitive: ${type(int1_input_sensitive)}
    - type_sensitive_int_argument1_sensitive:
        value: ${type(int1_input_sensitive)}
        sensitive: true
    - type_int_argument2_sensitive: ${type(int2_input_sensitive)}
    - type_sensitive_int_argument2_sensitive:
        value: ${type(int2_input_sensitive)}
        sensitive: true

    - type_float_argument1: ${type(float1_input)}
    - type_sensitive_float_argument1:
        value: ${type(float1_input)}
        sensitive: true
    - type_float_argument2: ${type(float2_input)}
    - type_sensitive_float_argument2:
        value: ${type(float2_input)}
        sensitive: true
    - type_float_argument1_sensitive: ${type(float1_input_sensitive)}
    - type_sensitive_float_argument1_sensitive:
        value: ${type(float1_input_sensitive)}
        sensitive: true
    - type_float_argument2_sensitive: ${type(float2_input_sensitive)}
    - type_sensitive_float_argument2_sensitive:
        value: ${type(float2_input_sensitive)}
        sensitive: true

    - type_str_argument1: ${type(str1_input)}
    - type_sensitive_str_argument1:
        value: ${type(str1_input)}
        sensitive: true
    - type_str_argument2: ${type(str2_input)}
    - type_sensitive_str_argument2:
        value: ${type(str2_input)}
        sensitive: true
    - type_str_argument1_sensitive: ${type(str1_input_sensitive)}
    - type_sensitive_str_argument1_sensitive:
        value: ${type(str1_input_sensitive)}
        sensitive: true
    - type_str_argument2_sensitive: ${type(str2_input_sensitive)}
    - type_sensitive_str_argument2_sensitive:
        value: ${type(str2_input_sensitive)}
        sensitive: true

    - type_list_argument1: ${type(list1_input)}
    - type_sensitive_list_argument1:
        value: ${type(list1_input)}
        sensitive: true
    - type_list_argument2: ${type(list2_input)}
    - type_sensitive_list_argument2:
        value: ${type(list2_input)}
        sensitive: true
    - type_list_argument1_sensitive: ${type(list1_input_sensitive)}
    - type_sensitive_list_argument1_sensitive:
        value: ${type(list1_input_sensitive)}
        sensitive: true
    - type_list_argument2_sensitive: ${type(list2_input_sensitive)}
    - type_sensitive_list_argument2_sensitive:
        value: ${type(list2_input_sensitive)}
        sensitive: true

  results:
    - SUCCESS
    - FAILURE
