#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops

flow:
  name: binding_scope_flow
  workflow:
    - step1:
        do:
          ops.binding_scope_op:
            - op_input_1: "op_input_1_step"
            - step_arg_1: "step_arg_1_value"
            - op_output_2_step: "op_output_2_step"
        publish:
          - step1_publish_1: ${ op_output_1 + ' ' + op_input_1 + ' ' + step_arg_1 }
          - step1_publish_2_conflict: ${ op_output_2 }
        navigate:
          - SUCCESS: SUCCESS
  results:
    - SUCCESS
