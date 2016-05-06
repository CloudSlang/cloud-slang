#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: compare_values
  inputs:
    - eq_str_bla
    - eq_int_22
    - eq_bool_true
  python_action:
    script: |
      result = (eq_str_bla == 'bla') and (eq_int_22 == 22) and (eq_bool_true)
  results:
    - SUCCESS: ${ result }
    - FAILURE