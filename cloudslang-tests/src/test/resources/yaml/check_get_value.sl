#   (c) Copyright 2015 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################

namespace: user.ops

imports:
  ops: user.ops

flow:
  name: check_get_value

  inputs:
    - map_before:
        default: >
          ${"{
          'outer_key1': 'outer_value1',
          'outer_key2': {'inner_key1': 'inner_value1', 'inner_key2': ['list_item1', 'list_item2']}
          }"}
    - map_key:
        default: "outer_key2"
    - found_value:
        default: "{'inner_key1': 'inner_value1', 'inner_key2': ['list_item1', 'list_item2']}"

  workflow:
    - get_value:
        do:
          get_value:
            - map_input: ${ map_before }
            - map_key
        publish:
          - value
        navigate:
          - SUCCESS: test_equality
          - FAILURE: CREATEFAILURE

    - test_equality:
        do:
          ops.check_equal_types:
            - first: ${ value }
            - second: ${ found_value }
        navigate:
          - EQUALS: SUCCESS
          - NOT_EQUALS: EQUALITY_FAILURE

  results:
    - SUCCESS
    - EQUALITY_FAILURE
    - CREATEFAILURE
