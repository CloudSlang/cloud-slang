#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: sensitive_system_properties_op
  inputs:
    - input1: ${get_sp('a.b.op.var1')}
    - input2: ${get_sp('a.b.op.var2')}
    - input3_sensitive: ${get_sp('a.b.op.var3_sensitive')}
  python_action:
    script: |
      language = 'CloudSlang'
  outputs:
    - output1: ${get_sp('a.b.op.var1')}
    - output2: ${get_sp('a.b.op.var2')}
    - output3_sensitive: ${get_sp('a.b.op.var3_sensitive')}
  results:
    - SUCCESS: ${get_sp('a.b.op.var1') == 'value op var1'}
    - FAILURE
