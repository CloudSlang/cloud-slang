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
  name: sensitive_system_properties_flow
  inputs:
    - input1: ${get_sp('a.b.flow.var1')}
    - input2: ${get_sp('a.b.flow.var2')}
    - input3_sensitive: ${get_sp('a.b.flow.var3_sensitive')}
  workflow:
    - step1:
        do:
          ops.sensitive_system_properties_op:
            - input1_for_step_only: ${get_sp('a.b.flow.var1')}
            - input2_for_step_only: ${get_sp('a.b.flow.var2')}
            - input3_for_step_only_sensitive: ${get_sp('a.b.flow.var3_sensitive')}
        publish:
          - publish1: ${get_sp('a.b.flow.var1')}
          - publish2: ${get_sp('a.b.flow.var2')}
          - publish3_sensitive: ${get_sp('a.b.flow.var3_sensitive')}
  outputs:
    - output1: ${get_sp('a.b.flow.var1')}
    - output2: ${get_sp('a.b.flow.var2')}
    - output3_sensitive: ${get_sp('a.b.flow.var3_sensitive')}
