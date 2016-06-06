#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

imports:
  ops: user.ops

flow:
  name: flow_missing_dependency_required_input_in_grandchild
  workflow:
    - explicit_alias:
        do:
          flow_implicit_alias_for_current_namespace:
    - implicit_alias:
        do:
          check_op:

