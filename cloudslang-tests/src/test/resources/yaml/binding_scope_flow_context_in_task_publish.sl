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
  name: binding_scope_flow_context_in_task_publish
  inputs:
    - flow_var: 'flow_var_value'
  workflow:
    - task1:
        do:
          ops.binding_scope_op:
            - op_input_1: "op_input_1_task"
            - task_arg_1: "task_arg_1_value"
            - op_output_2: "op_output_2_task"
        publish:
          - task1_publish_1: ${ flow_var }
