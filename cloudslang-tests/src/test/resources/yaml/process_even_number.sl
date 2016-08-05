#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: process_even_number
  inputs:
    - even_number
    - offset: '32'
  python_action:
    script: |
      processing_result = int(even_number) + int(offset)
      print 'Even number processed. Result= ' + str(processing_result)
