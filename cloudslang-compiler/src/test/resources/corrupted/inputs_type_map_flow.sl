#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
namespace: io.cloudslang

imports:
  opertions: io.cloudslang

flow:
  name: inputs_type_string_flow
  inputs:
    input1:
  workflow:
    - step1:
        do:
          ops.op1:
