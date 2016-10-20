#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops.parallel_loop

operation:
  name: print_list
  inputs:
     - words_list
  python_action:
    script: |
      if words_list != None and len(words_list) > 0:
          print words_list
  results:
    - SUCCESS: ${1==1}
    - FAILURE
