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
  name: loop_with_acc
  inputs:
    - values: "1,2,3"
    - loop_acc:
        default: ''
        required: false
        private: true
  workflow:
    - print_values:
        loop:
          for: "value in '1,2,3'"
          do:
            ops.do_nothing:
              - input_1: ${value}
              - loop_acc
          publish:
            - loop_acc: ${ loop_acc + ' ' + output_1 }
  outputs:
    - loop_result: ${loop_acc.strip()}
