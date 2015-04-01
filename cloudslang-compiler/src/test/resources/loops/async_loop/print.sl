#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops.async_loop

operation:
  name: print
  inputs:
     - text
  action:
    python_script: |
        name = 'branch ' + str(text)
        name_length = len(name)
        print 'Hello from ' + name
  outputs:
    - name
    - name_length
