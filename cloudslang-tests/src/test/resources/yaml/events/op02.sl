#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

operation:
  name: op02
  inputs:
    - op01_input_01
    - op01_input_02
    - op01_input_03
  python_action:
    script: |
      action_output_01 = 'out01'
      action_output_02 = 'out02'

      x = iDontExist
  outputs:
    - op01_output_01: ${op01_input_01}
    - op01_output_02: ${action_output_02}
    - op01_output_03: ${op01_input_03}
