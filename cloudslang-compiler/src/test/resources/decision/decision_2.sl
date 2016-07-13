#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.decisions

decision:
  name: decision_2
  inputs:
    - x
    - y
    - z:
        default: 'default_value'
        required: false
  results:
    - EQUAL: ${x == y}
    - LESS_THAN: ${x < y}
    - GREATER_THAN
