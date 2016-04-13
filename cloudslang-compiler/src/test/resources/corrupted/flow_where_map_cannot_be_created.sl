#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang

#imports map cannot be created since key-value pairs are not provided(there is no " " after ":")
imports:
 user:value
 slang:value
flow:
 name: flow_where_map_cannot_be_created

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