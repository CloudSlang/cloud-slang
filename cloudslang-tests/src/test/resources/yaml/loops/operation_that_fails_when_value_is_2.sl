#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0namespace: loops

namespace: loops

operation:
  name: operation_that_fails_when_value_is_2
  inputs: ['text']
  python_action:
    script: print text
  results:
    - FAILURE: ${ text == "2" }
    - SUCCESS