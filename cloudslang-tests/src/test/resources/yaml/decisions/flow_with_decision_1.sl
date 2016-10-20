#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops
  decisions: user.decisions

flow:
  name: flow_with_decision_1
  inputs:
    - x
    - y
  workflow:
    - op1:
        do:
          ops.noop: []
        navigate:
          - SUCCESS: compare

    - compare:
        do:
          decisions.decision_1:
            - x
            - y
        publish:
          - sum
        navigate:
          - EQUAL: EQUAL
          - LESS_THAN: LESS_THAN
          - GREATER_THAN: GREATER_THAN
  outputs:
    - sum
  results:
    - EQUAL
    - LESS_THAN
    - GREATER_THAN
