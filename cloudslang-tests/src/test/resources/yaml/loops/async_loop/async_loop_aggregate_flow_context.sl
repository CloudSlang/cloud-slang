#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops.async_loop

imports:
  ops: loops.async_loop

flow:
  name: async_loop_aggregate_flow_context
  inputs:
    - values: ${ range(1, 4) }
    - flow_var: 'FLOW VARIABLE VALUE'
  workflow:
    - print_values:
        async_loop:
          for: value in values
          do:
            ops.print_branch:
              - ID: ${ value }
          publish:
            - name
            - number: ${ int_output }
        aggregate:
            - output_from_flow_context: ${ flow_var }
