#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
  ops: user.ops

flow:
  name: system_property_dependencies_flow
  inputs:
    - input1
    - input2: "get_sp('flow.input.i_am_no_property')"
    - input3: ${get_sp('flow.input.prop1')}
    - input4:
        default: ${get_sp('flow.input.prop2')}
    - input5: ${get_sp('flow.input.prop3', 'default_str')}
    - input6: ${get_sp("flow.input.prop4")}
    - input7: ${get_sp('flow.input.prop5', 'default_str')}
    - input8: ${check_empty(get('i_dont_exist'), 'default_str')}
  workflow:
    - Task1:
        do:
          ops.system_property_dependencies_op:
            - input1
            - input2: "get_sp('task.input.i_am_no_property')"
            - input3: ${get_sp('task.input.prop1')}
            - input4: ${get_sp('task.input.prop2', 'default_str')}
            - input5: ${get_sp("task.input.prop3")}
            - input6: ${get_sp("task.input.prop4", 'default_str')}
            - input7: ${check_empty(get('i_dont_exist'), 'default_str')}
        publish:
            - publish_1
            - publish_2: "get_sp('task.publish.i_am_no_property')"
            - publish_3: ${get_sp('task.publish.prop1')}
            - publish_4: ${get_sp('task.publish.prop2', 'default_str')}
            - publish_5: ${get_sp("task.publish.prop3")}
            - publish_6: ${get_sp("task.publish.prop4", 'default_str')}
            - publish_7: ${check_empty(get('i_dont_exist'), 'default_str')}
        navigate:
          FUNCTIONS_KEY_EXISTS: Task2
          FUNCTIONS_KEY_EXISTS_PROBLEM: FUNCTIONS_KEY_EXISTS_PROBLEM
          
    - Task2:
        async_loop:
          for: value in [1,2,3]
          do:
            ops.system_property_dependencies_op: []
        aggregate:
            - aggregate_1
            - aggregate_2: "get_sp('async.aggregate.i_am_no_property')"
            - aggregate_3: ${get_sp('async.aggregate.prop1')}
            - aggregate_4: ${get_sp('async.aggregate.prop2', 'default_str')}
        navigate:
          FUNCTIONS_KEY_EXISTS: FUNCTIONS_KEY_EXISTS
          FUNCTIONS_KEY_EXISTS_PROBLEM: FUNCTIONS_KEY_EXISTS_PROBLEM
          
    - Task3:
        loop:
          for: value in [1,2,3]
          do:
            ops.system_property_dependencies_op:
              - input1
              - input2: "get_sp('for.input.i_am_no_property')"
              - input3: ${get_sp('for.input.prop1')}
              - input4: ${get_sp('for.input.prop2', 'default_str')}
          publish:
              - publish_1
              - publish_2: "get_sp('for.publish.i_am_no_property')"
              - publish_3: ${get_sp('for.publish.prop1')}
              - publish_4: ${get_sp('for.publish.prop2', 'default_str')}
        navigate:
          FUNCTIONS_KEY_EXISTS: FUNCTIONS_KEY_EXISTS
          FUNCTIONS_KEY_EXISTS_PROBLEM: FUNCTIONS_KEY_EXISTS_PROBLEM
  outputs:
    - output_1: ${get(    'i_don_exist',        get_sp('flow.output.prop1')       )}
  results:
    - FUNCTIONS_KEY_EXISTS
    - FUNCTIONS_KEY_EXISTS_PROBLEM
