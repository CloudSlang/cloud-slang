#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: cloudslang.external

flow:
  name: flow_with_external_step_by_uuid
  workflow:
    - step1:
        do_external:
          '2c22355a-9c26-4a9e-893c-01a35e4bb0e3':
            - min: '1'
            - max: '10'

  results:
        - SUCCESS
        - FAILURE