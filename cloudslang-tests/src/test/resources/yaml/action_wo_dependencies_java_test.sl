#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: action_wo_dependencies_java_test
  inputs:
    - host: 'localhost'
    - port: '8080'
  java_action:
    class_name: io.cloudslang.lang.systemtests.actions.LangTestActions
    method_name: parseUrl
  outputs:
    - url
  results:
    - SUCCESS: ${ url is not None }
    - FAILURE
