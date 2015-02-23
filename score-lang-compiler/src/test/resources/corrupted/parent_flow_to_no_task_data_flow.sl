#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
namespace: org.openscore

imports:
  flows: org.openscore

flow:
  name: parent_flow_to_missing_name_flow

  workflow:
    - missing_name_subflow:
        do:
          flows.no_task_data:
