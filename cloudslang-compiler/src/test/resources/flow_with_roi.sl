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
  name: flow_with_roi
  workflow:
    - main_step:
        do:
          ops.test_op:
            - city: 'sin'
            - alla: ':-)'
        navigate:
          - SUCCESS:
              next_step: SUCCESS
              ROI: 11
          - FAILURE:
              next_step: reset_step_on_failure

    - reset_step_on_failure:
        do:
          ops.test_op:
            - city: 'N/A'
            - alla: ':-('
        navigate:
          - SUCCESS:
              next_step: SUCCESS
              ROI: 1
          - FAILURE:
              next_step: FAILURE
              ROI: -1

  results:
    - SUCCESS
    - FAILURE