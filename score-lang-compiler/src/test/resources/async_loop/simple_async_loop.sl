#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: async_loop

imports:
  ops: async_loop

flow:
  name: simple_async_loop
  inputs:
    - values: [1,2,3]
  workflow:
    - print_values:
        async_loop:
          for: value in values
          do:
            ops.print:
              - text: value
