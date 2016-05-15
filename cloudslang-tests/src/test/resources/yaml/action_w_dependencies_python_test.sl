#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: action_w_dependencies_python_test
  python_action:
    dependencies:
      - 'some.group:some.artifact:some_version-1.1'
      - 'some.group1:some.artifact:some_version-2.1'
    script: 'print "hello world"'
