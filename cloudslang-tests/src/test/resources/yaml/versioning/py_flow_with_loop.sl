#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang.coreos

imports:
  ops: user.versioning.ops

flow:
  name: py_flow_with_loop

  inputs:
    - addedValue:
        required: true
  workflow:
    - loop_sum_of_mul_of_sum:
        parallel_loop:
          for: n in ('0','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19')
          do:
            ops.py_dependency_mul_op:
              - var1: ${n}
              - var2: ${addedValue}
        publish:
          - mul_concat: ${'+++'.join(map(lambda x:x['result_version_mul'], branches_context))}
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: FAILURE
  outputs:
    - muls_result: ${mul_concat}
  results:
    - SUCCESS
    - FAILURE
