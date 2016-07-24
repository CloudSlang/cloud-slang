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
  name: default_navigation_missing_result
  workflow:
    - jedi_training_1:
        do:
          ops.check_op: []

    - jedi_training_2:
        do:
          ops.check_op: []
        navigate:
          - SUCCESS: EQUAL
          - FAILURE: LESS_THAN
  results:
    - EQUAL
    - LESS_THAN
