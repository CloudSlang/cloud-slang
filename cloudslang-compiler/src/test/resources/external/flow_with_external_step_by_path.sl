#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: cloudslang.external

flow:
  name: flow_with_external_step_by_path
  workflow:
    - step1:
        do_external:
          'test/flow/Random Number Generator':
            - min: '1'
            - max: '10'

  results:
        - SUCCESS
        - FAILURE