#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0namespace: loops

namespace: loops

imports:
  ops: loops

flow:
  name: simple_loop_empty
  inputs:
    - values: "1,2,3"
  workflow:
    - print_values:
        loop:
          for:
          do:
            ops.print:
              - text: "text"
              - sp_arg: ${get_sp('loop.for.prop1')}
