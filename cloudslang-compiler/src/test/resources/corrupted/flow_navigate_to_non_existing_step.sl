#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

flow:
 name: flow_navigate_to_non_existing_step

 workflow:
   - Step1:
       do:
         user.ops.test_op:
            - alla: ${"temp"}
       navigate:
         - SUCCESS: non_existing_step
         - FAILURE: FAILURE
 results:
  - SUCCESS
  - FAILURE