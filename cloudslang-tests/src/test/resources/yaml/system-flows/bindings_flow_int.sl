#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: system.flows

imports:
 ops: system.ops

flow:
  name: bindings_flow_int
  inputs:
    - base_input
    - bound_input:
        default: ${ base_input + 1 }

  workflow:
    - Step_1:
        do:
          ops.binding_flow_int_op:
            - base_input: ${ bound_input + 1 }
        publish:
          - bound_input: ${ bound_output + 1 }
        navigate:
          - SUCCESS: Step_2

    - Step_2:
        do:
          ops.binding_flow_int_op:
            - base_input: ${ bound_input + 1 }
        publish:
          - bound_input: ${ bound_output + 1 }
        navigate:
          - SUCCESS: SUCCESS

  outputs:
    - final_output: ${ bound_input + 1 }
  results:
    - SUCCESS
