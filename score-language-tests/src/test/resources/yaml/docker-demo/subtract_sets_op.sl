#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

####################################################
#
#   This operation will subtract set2 from set1, meanning return result will be : set1 - set2
#
####################################################
namespace: lists.manipulation


operations:
  - subtract_sets:
      inputs:
        - set_1
        - set_1_delimiter
        - set_2
        - set_2_delimiter
        - result_set_delimiter
      action:
        python_script: |
          arr_list_1 = set_1.split(set_1_delimiter)
          arr_list_2 = set_2.split(set_2_delimiter)

          result =  set(arr_list_1) - set(arr_list_2)

          result_set = result_set_delimiter.join(result)
      outputs:
        - result_set
      results:
        - SUCCESS
        - FAILURE