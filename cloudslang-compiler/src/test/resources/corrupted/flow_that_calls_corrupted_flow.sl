#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

flow:
  name: flow_that_calls_corrupted_flow
  workflow:
    - call_to_other_flow:
        do:
          flow_input_in_step_same_name_as_dependency_output:
            - input1