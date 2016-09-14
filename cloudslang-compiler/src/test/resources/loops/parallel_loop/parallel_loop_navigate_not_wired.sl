#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops.parallel_loop

imports:
  ops: loops.parallel_loop

flow:
  name: parallel_loop_navigate_not_wired
  inputs:
    - values: ${ range(1, 11) }
  workflow:
    - print_values:
        parallel_loop:
          for: value in values
          do:
            ops.print_branch:
              - ID: ${ value }
        navigate:
          - SUCCESS: print_list

    - print_list:
        do:
            ops.print_list:
                - words_list: '[]'
