#   (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
#!!
#! @description: Sorts the elements in a list.
#! @input input_list: type of list (INT or STR) - Example: [123 'zyx']
#! @output result: sorted list of int or string elements
#! @result SUCCESS: sorting successfull
#!!#
####################################################

namespace: io.cloudslang

operation:
  name: check_sort_list
  inputs:
    - input_list: "element5,element3,element4,element2,element7,element1,element6"

  python_action:
    script: |
      in_list = input_list.split(",")
      if all(isinstance(item, int) for item in in_list):
        result = sorted(in_list)
      elif all(isinstance(item, basestring) for item in in_list):
        sorted_list = sorted(in_list)
        string_list = []
        for item in sorted_list:
          string_list.append(str(item))
          result = str(string_list)

  outputs:
    - result

  results:
    - SUCCESS
