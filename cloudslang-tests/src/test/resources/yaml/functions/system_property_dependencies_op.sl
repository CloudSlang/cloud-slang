#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: system_property_dependencies_op
  inputs:
    - input1
    - input2: "get_sp('op.input.i_am_no_property')"
    - input3: ${get_sp('op.input.prop1')}
    - input4:
        default: ${get_sp('op.input.prop2')}
    - input_5: ${get_sp('op.input.prop3', 'default_str')}
    - input6: ${get_sp("op.input.prop4")}
    - input7: ${get_sp("op.input.prop5", 'default_str')}
    - input8: ${check_empty(get('i_dont_exist'), 'default_str')}
  python_action:
    script: |
      language = 'CloudSlang'
  outputs:
    - output_7: ${get(    'i_don_exist',        get_sp('op.output.prop1')       )}
  results:
    - FUNCTIONS_KEY_EXISTS: ${ get(get_sp('op.result.prop1'), 'output1_default') == 'CloudSlang' }
    - FUNCTIONS_KEY_EXISTS_PROBLEM
