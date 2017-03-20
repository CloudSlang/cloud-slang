#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: loops

imports:
  ops: loops

flow:
  name: for_loop_step_modifiers
  inputs:
    - values: "1,2,3"
  workflow:
    - print_values:
        loop:
          for: x in values.split(",")
          do:
            ops.print:
              - step_input_01:
                  value: ${ x }
              - step_input_02
              - step_input_03:
                  value: ${ step_input_03_value }
                  sensitive: false
              - step_input_04:
                  value: ${ step_input_04_value }
                  sensitive: true
          publish:
            - new_var: 'a'
