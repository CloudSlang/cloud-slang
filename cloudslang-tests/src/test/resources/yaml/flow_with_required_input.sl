#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops
imports:
  ops: user.ops
flow:
  inputs:
    - input:
        required: true
  name: flow_with_required_input
  workflow:
    - print:
        do:
          ops.print:
            - text: ${input}
        publish:
          - printed_text
        navigate:
          - SUCCESS: SUCCESS
  outputs:
    - returnResult: ${input}
    - printed_text
  results:
    - SUCCESS
