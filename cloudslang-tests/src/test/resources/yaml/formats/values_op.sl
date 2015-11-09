#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: values_op
  inputs:
    # used in output binding
    - output_no_expression: output_no_expression_value
  action:
    python_script: 'pass'
  outputs:
    - output_no_expression
    - output_int: 22
    - output_str: output_str_value
    - output_expression: ${ output_str + '_suffix' }
