
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
  name: flow02
  inputs:
    - flow01_input01
    - flow01_input02:
        default: 'abc'
        private: true
    - flow01_input03:
        sensitive: true
  workflow:
    - step1:
        do:
          cs.op02:
            - op01_input_01: 'def'
            - op01_input_02: ${flow01_input02}
            - op01_input_03: ${flow01_input03}
        publish:
          - step01_publish_01: ${op01_output_01}
          - step01_publish_02: ${op01_output_02}
          - step01_publish_03: ${op01_output_03}
        navigate:
          - SUCCESS: step2

    - step2:
        parallel_loop:
          for: x in 1, 2, 3
          do:
            cs.op02:
              - op01_input_01: ${str(x)}
              - op01_input_02: ${flow01_input02}
              - op01_input_03: ${flow01_input03}
        publish:
          - parallel_loop_output: 'efg'
        navigate:
          - SUCCESS: SUCCESS
  results:
   - SUCCESS
