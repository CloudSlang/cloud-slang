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
  name: loop_from_property_with_custom_navigation
  inputs:
    - values: "1,2"
  workflow:
    - print_values:
        loop:
          for: value in get_sp('loops.list').split(",")
          do:
            ops.print:
              - text: ${ value }
        navigate:
          - SUCCESS: print_other_values
          - FAILURE: FAILURE

    - print_other_values:
        do:
          ops.print:
            - text: 'abc'