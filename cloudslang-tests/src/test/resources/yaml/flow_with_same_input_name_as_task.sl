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
  name: flow_with_same_input_name_as_task
  inputs:
    - first # normal input
    - second_string # same as task input
  workflow:
    - CheckBinding:
        do:
          ops.string_equals:
            - first_string: "'prefix_' + first"
            - second_string:
                default: >
                  'prefix_' + second_string
                overridable: false
        navigate:
          SUCCESS: TaskOnSuccess
          FAILURE: TaskOnFailure

    - TaskOnFailure:
        do:
          ops.test_op:

    - TaskOnSuccess:
        do:
          ops.test_op:
