#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

imports:
  ops: user.ops

flow:
  name: on_failure_reachable_result
  workflow:
    - print_message1:
        do:
          ops.test_op:
            - alla: 'message 1'
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: on_failure
    - on_failure:
        - print_on_failure_1:
            do:
              ops.test_op:
                - alla: 'on_failure 1'
  results:
    - SUCCESS
    - FAILURE
