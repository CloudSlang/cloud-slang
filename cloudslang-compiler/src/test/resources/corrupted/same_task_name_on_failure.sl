#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops

flow:
  name: same_task_name_on_failure
  workflow:
    - other_task:
        do:
          ops.fail_on_input_op: [nr: 1]

    - on_failure:
        - task_same_name_on_failure:
            do:
              ops.fail_on_input_op: [nr: 0]

        - task_same_name_on_failure:
            do:
              ops.fail_on_input_op: [nr: 0]
