#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
namespace: user.ops

imports:
  ops: user.ops

flow:
  name: flow_with_missing_navigation_from_op_result

  workflow:
    - Step1:
        do:
          ops.print_custom_result_op:

  results:
    - SUCCESS
    - FAILURE
