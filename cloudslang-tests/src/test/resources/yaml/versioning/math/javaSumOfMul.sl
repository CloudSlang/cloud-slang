#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.versioning.math.ops

operation:
  name: javaSumOfMul
  inputs:
    - var1:
        required: true
    - var2:
        required: true
  java_action:
    gav: 'math:sum_of_mul:1.0'
    class_name: utils.complex.Compute
    method_name: compute
  outputs:
    - result: ${ str(result) }
  results:
    - SUCCESS
