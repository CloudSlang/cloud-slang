#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0namespace: loops

namespace: loops

imports:
  ops: loops

flow:
  name: loop_with_break_with_map
  inputs:
    - person_map: > # make all the values 2 to be sure that after the first key-value pair the loop will break
        ${{'john': 2, 'jane': 2, 'peter': 2}}
  workflow:
    - print_values:
        loop:
          for: k, v in person_map
          do:
            ops.operation_that_goes_to_custom_when_value_is_2:
              - text: ${ v }
          break:
            - CUSTOM
        navigate:
          - CUSTOM: print_other_values
          - SUCCESS: SUCCESS

    - print_other_values:
        do:
          ops.print:
            - text: 'abc'