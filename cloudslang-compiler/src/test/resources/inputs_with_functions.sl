#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: inputs_with_functions
  inputs:
    - input1
    - input2: ${1 + 2}
    - input3: ${get('key', 'value')}
    - input4: ${get_sp('a.b.c.key')}
    - input5: ${get_sp('a.b.c.key', 'DEFAULT')}
    - input6: ${get('key', get_sp('a.b.c.key'))}
    - input7:
        default: ${get('key', get_sp('a.b.c.key'))}
    - input8:
        default: ${get('key', get_sp('a.b.c.key')) + get_sp('d.e.f.key')}
    - input9:
        default: ${get('key', get_sp('a.b.c.key')) + get_sp('a.b.c.key')}
    - input10:
        default: ${get_sp(expression)}
    - input11:
        default: ${get_sp( 'a.b.c.key'     )}
    - input12: ${get('key1', 'value1') + get('key2', 'value2')}
    - input13: ${get_sp('a.b.c.key', 'value1') + get_sp('d.e.f.key', 'value2')}
    - input14: ${check_empty(get('i_dont_exist'), 'DEFAULT')}
  action:
    python_script: pass
