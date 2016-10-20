#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.flows

imports:
 ops: user.ops

flow:
  name: on_failure_not_defined
  inputs:
    - navigationType
    - emailHost
    - emailPort
    - emailSender
    - emailRecipient
  workflow:
    - produce_default_navigation:
        do:
          ops.produce_default_navigation:
            - navigationType
        publish:
          - default_output
        navigate:
          - SUCCESS: SUCCESS
          - FAILURE: on_failure
  outputs:
    - default_output
