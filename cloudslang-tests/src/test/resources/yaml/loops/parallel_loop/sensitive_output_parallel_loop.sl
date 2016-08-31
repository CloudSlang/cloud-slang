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
  name: sensitive_output_parallel_loop
  inputs:
    - values: "1,2,3"
  workflow:
    - print_values:
        parallel_loop:
          for: value in values.split(",")
          do:
            ops.print_branch:
              - ID: ${ value }
        publish:
          - name_list: ${ str(map(lambda x:str(x['name']), branches_context)) }
          - number_from_last_branch: ${ branches_context[-1]['int_output'] }
          - from_sp: ${get_sp('loop.parallel.prop1')}
  outputs:
    - value1: ${values[0]}
    - value2:
        value: ${values[1]}
        sensitive: false
    - value3:
        value: ${values[2]}
        sensitive: true
