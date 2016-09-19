#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0namespace: loops

namespace: loops

imports:
  ops: loops.parallel_loop

flow:
  name: parallel_loop_with_inline_map
  workflow:
    - print_values:
        parallel_loop:
          for: k, v in {'1':1,'2':'jane','3':'peter'}
          do:
            ops.print_branch_map:
              - key: ${ k }
              - value: ${ str(v) }
        publish:
          - name_list: ${ str(map(lambda x:str(x['name']), branches_context)) }
          - number_from_last_branch: ${ branches_context[-1]['int_output'] }
          - from_sp: ${get_sp('loop.parallel.prop1')}
