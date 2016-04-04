#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops.async_loop

imports:
  ops: loops.async_loop

flow:
  name: async_loop_aggregate_navigate
  inputs:
    - values: ${ range(1, 11) }
  workflow:
    - print_values:
        async_loop:
          for: value in values
          do:
            ops.print_branch:
              - ID: ${ value }
          publish:
            - name
            - number: ${ int_output }
        aggregate:
            - name_list: ${ map(lambda x:str(x['name']), branches_context) }
            - number_from_last_branch: ${ branches_context[-1]['number'] }
        navigate:
            - SUCCESS: print_list
            - FAILURE: FAILURE

    - print_list:
        do:
            ops.print_list:
                - words_list: ${ [name_list, number_from_last_branch] }
