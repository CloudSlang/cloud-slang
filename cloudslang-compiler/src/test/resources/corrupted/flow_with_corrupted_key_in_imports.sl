#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

#second key from imports map does not contain value (there is no " " after ":")
imports:
 ops: user.ops
 flows:slang.sample.flows
flow:
 name: flow_with_corrupted_key_in_imports

 workflow:
   - Step1:
       do:
         ops.java_op:
       navigate:
         SUCCESS: SUCCESS
         FAILURE: FAILURE
   - Step2:
       do:
         flows.SimpleFlow:
           - city_name: 'New York'
       navigate:
         SUCCESS: SUCCESS
         FAILURE: FAILURE