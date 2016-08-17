#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops

flow:
  name: flow_with_extensions_tag
  workflow:
    - noop_step:
        do:
          ops.noop: []
        navigate:
          - SUCCESS: SUCCESS
  results:
    - SUCCESS

extensions:
  - visual:
      a: b
      c: d
  - hello