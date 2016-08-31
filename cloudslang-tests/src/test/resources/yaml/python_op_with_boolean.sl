#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: python_op_with_boolean
  python_action:
    script: |
      condition_1 = True
      condition_2 = 1!=1
      condition_3 = 1==1 and False
      condition_4 = 1<>1 or bool(1)
      an_int = 1
  outputs:
    - condition_1: ${ str(condition_1) }
    - condition_2: ${ str(condition_2) }
    - condition_3: ${ str(condition_3) }
    - condition_4: ${ str(condition_4) }
    - an_int: ${ str(an_int) }
  results:
    - SUCCESS