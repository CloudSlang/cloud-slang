#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: binding_scope_op
  inputs:
    - op_input_1: "op_input_1_value"
  python_action:
    script: pass
  outputs:
    - op_output_1: 'op_output_1_value'
    - op_output_2: 'op_output_2_value'
  results:
    - SUCCESS
