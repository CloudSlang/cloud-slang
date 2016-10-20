#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops.parallel_loop

operation:
  name: print_branch_map
  inputs:
     - key
     - value
  python_action:
    script: |
      name = 'branch ' + str(key)
      int_output = int(key)
      print 'Hello from ' + name + ' ' + value
  outputs:
    - name
    - int_output: ${ str(int_output) }
  results:
    - SUCCESS: ${1==1}
    - FAILURE