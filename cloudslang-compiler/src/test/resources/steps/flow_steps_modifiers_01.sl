#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
namespace: io.cloudslang

imports:
  cs: io.cloudslang

flow:
  name: flow_steps_modifiers_01
  workflow:
    - step_01:
        do:
          cs.op_01:
            - input_01: 'input_01_value'
            - input_02:
                value: 'input_02_value'
            - input_03:
                sensitive: false
            - input_04
            - input_05:
                value: 'input_05_value'
                sensitive: false
            - input_06:
                sensitive: true
            - input_07:
                value: 'input_07_value'
                sensitive: true
            - input_08:
                value: "${get_sp('a.b.c.sp0')}"
                sensitive: false
            - input_09:
                value: "${get(get_sp('a.b.c.sp0'), 'default_value')}"
                sensitive: true
        publish:
          - output_01
          - publish_02: ${output_02}
          - publish_03: 'publish_03_value'
        navigate:
          - SUCCESS: step_02
          - FAILURE: FAILURE

    - step_02:
        do:
          cs.op_02:
            - input_01: 'input_01_value'
        publish:
          - output_01
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: FAILURE
  results:
    - SUCCESS
    - FAILURE
