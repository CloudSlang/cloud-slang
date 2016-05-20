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
  name: parallel_loop_publish_on_branch
  inputs:
    - values: ${ range(1, 11) }
  workflow:
    - print_values:
        parallel_loop:
          for: value in values
          do:
            ops.print_branch:
              - ID: ${ value }
          publish:
            - branch_output: 'branch_output_value'
        publish:
            - name_list: ${ map(lambda x:str(x['name']), branches_context) }
            - number_from_last_branch: ${ branches_context[-1]['number'] }
