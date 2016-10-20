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
    - Step1:
        do:
          ops.system_property_dependencies_op:
            - input1
            - input2: "get_sp('step.input.i_am_no_property')"
            - input3: ${get_sp('step.input.prop1')}
            - input4: ${get_sp('step.input.prop2', 'default_str')}
            - input5: ${get_sp("step.input.prop3")}
            - input6: ${get_sp("step.input.prop4", 'default_str')}
            - input7: ${check_empty(get('i_dont_exist'), 'default_str')}
        publish:
            - publish_1
            - publish_2: "get_sp('step.publish.i_am_no_property')"
            - publish_3: ${get_sp('step.publish.prop1')}
            - publish_4: ${get_sp('step.publish.prop2', 'default_str')}
            - publish_5: ${get_sp("step.publish.prop3")}
            - publish_6: ${get_sp("step.publish.prop4", 'default_str')}
            - publish_7: ${check_empty(get('i_dont_exist'), 'default_str')}
        navigate:
          - FUNCTIONS_KEY_EXISTS: Step2
          - FUNCTIONS_KEY_EXISTS_PROBLEM: FUNCTIONS_KEY_EXISTS_PROBLEM
          
    - Step2:
        parallel_loop:
          for: value in [1,2,3]
          do:
            ops.system_property_dependencies_op:
                - input1: 'kuku'
        publish:
            - publish_1
            - publish_2: "get_sp('parallel_loop.publish.i_am_no_property')"
            - publish_3: ${get_sp('parallel_loop.publish.prop1')}
            - publish_4: ${get_sp('parallel_loop.publish.prop2', 'default_str')}
        navigate:
          - SUCCESS: Step3

    - Step3:
        loop:
          for: value in [1,2,3]
          do:
            ops.system_property_dependencies_op:
              - input1
              - input2: "get_sp('for.input.i_am_no_property')"
              - input3: ${get_sp('for.input.prop1')}
              - input4: ${get_sp('for.input.prop2', 'default_str')}
          break:
            - FUNCTIONS_KEY_EXISTS_PROBLEM
          publish:
              - publish_1
              - publish_2: "get_sp('for.publish.i_am_no_property')"
              - publish_3: ${get_sp('for.publish.prop1')}
              - publish_4: ${get_sp('for.publish.prop2', 'default_str')}
        navigate:
          - FUNCTIONS_KEY_EXISTS: FUNCTIONS_KEY_EXISTS
          - FUNCTIONS_KEY_EXISTS_PROBLEM: FUNCTIONS_KEY_EXISTS_PROBLEM
  outputs:
    - output_1: ${get(    'i_don_exist',        get_sp('flow.output.prop1')       )}
  results:
    - FUNCTIONS_KEY_EXISTS
    - FUNCTIONS_KEY_EXISTS_PROBLEM
