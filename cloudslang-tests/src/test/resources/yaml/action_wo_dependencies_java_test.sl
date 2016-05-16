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
  action:
    java_action:
      className: io.cloudslang.lang.systemtests.actions.LangTestActions
      methodName: parseUrl
  outputs:
    - url
  results:
    - SUCCESS: ${ url is not None }
    - FAILURE
