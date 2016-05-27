#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.testglobals.ops

operation:
  name: get_global_session_object_dependencies
  inputs:
    - value
  java_action:
    gav: 'cclassloaders:test_context:1.0-SNAPSHOT'
    class_name: context.SomeContext
    method_name: getConnectionFromNonSerializableSession
  outputs:
    - session_object_value: ${ connection }
  results:
    - SUCCESS: ${ connection == value }
    - FAILURE
