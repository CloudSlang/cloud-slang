#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
#!!
#! @description: Compares two inputs for Python equality (==).
#! @input first: first Python object to compare
#! @input second: second Python object to compare
#! @result EQUAL: object are equal
#! @result NOT_EQUAL: objects are not equal
#!!#
####################################################

namespace: user.ops

operation:
  name: check_equal_types
  inputs:
    - first
    - second
  python_action:
    script: |
      o1 = str(type(first))
      o2 = str(type(second))
      eq = first == second
  outputs:
    - eq: ${ str(eq) }
    - o1
    - o2
  results:
    - EQUALS: ${ bool(eq) }
    - NOT_EQUALS
