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
  name: simple_loop
  inputs:
    - values: "1,2,3"
  workflow:
    - print_values:
        loop:
          for: value in values.split(",")
          do:
            ops.print:
              - text: ${ value }
          publish:
            - new_var: 'a'