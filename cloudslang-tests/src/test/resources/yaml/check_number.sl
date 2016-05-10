#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: check_number
  inputs:
    - number
  python_action:
    script: |
      remainder = int(number) % 2
      isEven = remainder == 0
      tooBig = int(number) > 512
  outputs:
    - preprocessed_number: ${ str(int(number) * 3) }
  results:
    - EVEN: ${ isEven and not tooBig }
    - ODD: ${ not(isEven or tooBig) }
    - FAILURE # report failure if the number is too big
