#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

imports:
  ops: user.ops

flow:
  name: flow_with_custom_result
  inputs:
    - alla
  workflow:
    - print_message1_flow_with_custom_result:
        do:
          ops.test_op:
            - alla: alla
        navigate:
          - SUCCESS: CUSTOM_SUCCESS
  results:
    - CUSTOM_SUCCESS
