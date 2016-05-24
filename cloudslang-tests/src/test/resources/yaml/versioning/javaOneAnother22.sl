#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.versioning.ops

operation:
  name: javaOneAnother22
  java_action:
    gav: 'cloudslang.java:one:2.2'
    class_name: group.artifact.OneClass
    method_name: getVersion
  outputs:
    - version
  results:
    - SUCCESS
