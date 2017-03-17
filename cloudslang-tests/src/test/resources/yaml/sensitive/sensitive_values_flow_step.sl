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
  name: sensitive_values_flow_step
  inputs:
    - input_no_value_modifier:
        default: "input_no_value_modifier_value"
        sensitive: false
    - input_transitivity:
        default: "input_transitivity_value"
        sensitive: true
  workflow:
    - get_data:
        do:
          ops.noop:
            - input_no_value_modifier:
                sensitive: true # becomes sensitive by modifier
            - input_transitivity:
                sensitive: false # transitivity cannot be overridden
        navigate:
          - SUCCESS: prepare_for_print

    - prepare_for_print:
        do:
          ops.noop:
        navigate:
          - SUCCESS: SUCCESS
  results:
    - SUCCESS
