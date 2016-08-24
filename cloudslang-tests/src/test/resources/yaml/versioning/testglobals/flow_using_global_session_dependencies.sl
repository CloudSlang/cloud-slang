#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.testglobals.flows

imports:
  ops: user.testglobals.ops

flow:
  name: flow_using_global_session_dependencies
  inputs:
    - object_value
  workflow:
    - set_session_object:
        do:
          ops.set_global_session_object_dependencies:
            - value: ${ object_value }
        navigate:
          - SUCCESS: get_session_object

    - get_session_object:
        do:
          ops.get_global_session_object_dependencies:
            - value: ${ object_value }
        publish:
          - result_object_value: ${ session_object_value }
  outputs:
    - result_object_value
  results:
    - SUCCESS
    - FAILURE