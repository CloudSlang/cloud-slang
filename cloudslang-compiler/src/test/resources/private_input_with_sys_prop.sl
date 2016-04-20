#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: private_input_with_sys_prop
  inputs:
    - input_without_non_overidable_sys_prop:
        private: true
        system_property: booya
  action:
    python_script: print "hi"
