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
  name: simple_loop_with_inline_map
  workflow:
    - print_values:
        loop:
          for: k, v in {'john':1,'jane':'2','peter':'three'}
          do:
            ops.print:
              - text: ${ k }
              - text2: ${ str(v) }
          publish:
            - new_var: 'a'
