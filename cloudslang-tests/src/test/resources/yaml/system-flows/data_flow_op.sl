#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: system.ops

operation:
  name: data_flow_op
  inputs:
    - opMessage
    - unchangableMessage:
        default:  'defaultString'
        private: true
    - message: ${ opMessage }

  python_action:
    script: |
      if message != 'hello world':
        returnCode = '-1'
      elif unchangableMessage != 'defaultString':
        returnCode = '-1'
      else:
        returnCode = '0'
  outputs:
    - returnCode
  results:
    - SUCCESS: ${ returnCode == '0' }
    - FAILURE


