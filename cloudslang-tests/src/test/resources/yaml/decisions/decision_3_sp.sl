#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.decisions

decision:
  name: decision_3_sp
  inputs:
    - x: ${int(get_sp('user.sys.prop1'))}
    - y:
        default: ${int(get_sp('user.sys.prop2'))}
        required: false
  outputs:
    - sum: ${int(get_sp('user.sys.prop3'))}
  results:
    - EQUAL: ${x == int(get_sp('user.sys.prop4'))}
    - LESS_THAN: ${x < y}
    - GREATER_THAN
