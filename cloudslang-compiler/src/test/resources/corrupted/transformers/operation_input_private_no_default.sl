#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: operation_input_private_no_default
  inputs:
    - input1
    - input2: ${ input2 }
    - input3:
        default: 'value3'
    - input4: ${ 'value4' if input3 == value3 else None }
    - input_private_no_default:
        private: true
    - input5:
        required: yes
        sensitive: true
    - input6:
        default: ${ 1 + 5 }
        required: False
  python_action:
    script: pass
  results:
    - SUCCESS: ${ 1 == 1 }
    - FAILURE
