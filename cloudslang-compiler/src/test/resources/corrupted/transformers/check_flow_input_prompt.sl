#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops
  flows: user.flows

flow:
  name: check_flow_input_prompt
  inputs:
    - flow_input_1:
        default: "defaultValue"
        prompt:
          type: "text"
          message: "non-default-message"
    - flow_input_2:
        prompt:
          type: "text"
    - flow_input_3:
        prompt:
          type: "single-choice"
          options: "opts"
          delimiter: "|"
    - flow_input_4:
        prompt:
          type: "multi-choice"
          options: "opts"
  workflow:
    - bootstrap_node:
        do:
          ops.check_flow_input_prompt:
        navigate:
          - FAILURE: on_failure
          - SUCCESS: SUCCESS
  results:
    - FAILURE
    - SUCCESS
