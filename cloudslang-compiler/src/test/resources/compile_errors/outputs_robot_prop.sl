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
  name: outputs_robot_prop
  outputs:
    - output1:
        robot: false
  workflow:
    - step1:
        do:
          ops.print:
            - text: 'input_1'
