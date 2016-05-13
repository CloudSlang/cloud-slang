#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
namespace: io.cloudslang

imports:
  flows: io.cloudslang

flow:
  name: parent_flow_to_no_step_data_flow

  workflow:
    - missing_name_subflow:
        do:
          flows.no_step_data:
