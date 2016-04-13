#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

#cannot map values here because of the indentation of the first key from imports map
imports:
ops: user.ops
 flows: slang.sample.flows
flow:
 name: flow_with_missing_space_before_first_import

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