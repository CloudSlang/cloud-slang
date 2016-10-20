#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops

operation:
  name: do_nothing
  inputs:
     - input_1
     - input_2:
         default: 'input value2'
     - input3:
         default: 'input value3'
         private: true
  python_action:
    script: |
      pass
  outputs:
    - output_1: ${ input_1 }
    - output_2: 'output value 2'
  results:
    - SUCCESS: ${ 1 == 1 }
    - FAILURE
