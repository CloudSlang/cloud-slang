#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: operation_output_wrong_property
  python_action:
    script: pass
  outputs:
    - output_1: 'abc'
    - output_wrong_key:
        wrong_key: 'abc'
    - output_2: 'abc'
  results:
    - SUCCESS: ${ 1 == 1 }
    - FAILURE
