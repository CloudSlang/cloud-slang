
#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

operation:
  name: input_private_not_req_null
  inputs:
    - input1
    - input2:
        default: None
        required: false
        private: true
    - input3
  python_action:
    script: pass
  results:
    - SUCCESS: ${ 1 == 1 }
    - FAILURE
