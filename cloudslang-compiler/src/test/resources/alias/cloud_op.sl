#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: cloud

operation:
  name: cloud_op
  inputs:
    - cool_input: '1992'
  python_action:
    script: 'print "hello world"'
  results:
    - SUCCESS: ${1==1}
    - FAILURE
