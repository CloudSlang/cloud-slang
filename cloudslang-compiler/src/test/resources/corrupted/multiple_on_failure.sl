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
  name: multiple_on_failure
  workflow:
    - print_message:
        do:
          ops.test_op:
            - alla: 'hello'

    - on_failure:
        - print_on_failure_1:
            do:
              ops.test_op:
                - alla: 'on_failure 1'

    - on_failure:
        - print_on_failure_2:
            do:
              ops.test_op:
                - alla: 'on_failure 2'
