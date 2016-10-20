#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.versioning.math.flows

imports:
  ops: user.versioning.math.ops

flow:
  name: java_flow
  inputs:
    - var1:
        required: true
    - var2:
        required: true
  workflow:
    - step_mul_of_sum:
        do:
          ops.javaMulOfSum:
            - var1
            - var2
        publish:
          - result_mul_of_sum: ${result}
    - step_sum_of_mul:
        do:
          ops.javaSumOfMul:
            - var1
            - var2
        publish:
          - result_sum_of_mul: ${result}
  outputs:
    - result_mul_of_sum
    - result_sum_of_mul
  results:
    - SUCCESS
    - FAILURE
