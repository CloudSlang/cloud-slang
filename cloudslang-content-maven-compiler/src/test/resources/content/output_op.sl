#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################

namespace: base

operations:
  name: output_op
  inputs:
    - text
  action:
    python_script: |
      output_text1=text
  outputs:
    - output_text1
    - output_text2: 'second output'
  results:
    - SUCCESS: ${ text != "" }
    - FAILURE