#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: test_op_2
  inputs:
    - input1
    - input2: ${ input2 }
    - input3:
        default: 'value3'
    - input4: ${ 'value4' if input3 == value3 else None }
    - input5:
        required: yes
        sensitive: true
    - input6:
        default: ${ str(1 + 5) }
        required: False
    - input7: '77'
    - input8:
        default: ${ input6 }
    - input9:
        default: ${ input6 }
        private: true
    - input10:
        default: 'true'
        required: False

  python_action:
    script: |
      # this is python amigos!!
      import os
      processId = os.getpid()
      print processId
      print input1
      input1 = "new input"
      print input1
      print input2
      print input3
      print input4
      output4 = input4
      print input5
      print input6
  outputs:
    - output1: ${ input1 }
    - output2: ${ str(processId) }
    - output4
  results:
    - SUCCESS: ${ 1 != 123456 }
    - NO_ACTION: ${True}
    - FAILURE
