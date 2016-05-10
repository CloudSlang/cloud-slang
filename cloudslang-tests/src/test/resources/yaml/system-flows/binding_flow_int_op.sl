#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: system.ops

operation:
  name: binding_flow_int_op
  inputs:
    - base_input
    - bound_input:
        default: ${ base_input + 1 }

  python_action:
    script: |
      bound_result = bound_input + 1
  outputs:
    - bound_output: ${ bound_result + 1 }
  results:
    - SUCCESS


