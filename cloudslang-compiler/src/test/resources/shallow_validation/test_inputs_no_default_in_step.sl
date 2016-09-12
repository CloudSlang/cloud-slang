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
  name: test_inputs_no_default_in_step
  workflow:
    - explicit_alias:
        do:
          ops.test_op:
            - city: 'input_1'
            - port: '22'
            - alla: ''
