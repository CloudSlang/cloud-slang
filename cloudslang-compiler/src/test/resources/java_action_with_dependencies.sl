#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: java_action_with_dependencies
  java_action:
    gav: 'g:a:v'
    class_name: com.hp.thing
    method_name: someMethod
  results:
    - SUCCESS
