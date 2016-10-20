#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops.parallel_loop

operation:
  name: print_branch_custom_only
  inputs:
     - ID
  python_action:
    script: |
        name = 'branch ' + str(ID)
        int_output = len(name) + int(ID)
        print 'Hello from ' + name
  outputs:
    - name
    - int_output
  results:
    - CUSTOM
