#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.versioning.ops

operation:
  name: py_dependency_sum5_op
  inputs:
    - var1:
        required: true
    - var2:
        required: true
  python_action:
    dependencies:
      - 'cloudslang.release:ver:sum-5.0'
    script: |
      import ver.utils.getver as ver
      ver_label = ver.get_ver(int(var1),int(var2))
  outputs:
    - version_sum5: ${ str(ver_label) }
  results:
    - SUCCESS

