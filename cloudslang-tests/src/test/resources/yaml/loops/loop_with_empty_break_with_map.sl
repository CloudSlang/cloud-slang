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
  name: loop_with_empty_break_with_map
  inputs:
    - person_map: > # each step will end with FAILURE but the loop will not break on the result
        ${{'john': 2, 'jane': 2, 'peter': 2}}
  workflow:
    - print_values:
        loop:
          for: k, v in person_map
          do:
            ops.operation_that_fails_when_value_is_2:
              - text: ${ v }
          break: []
