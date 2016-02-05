#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
#####################################################
#!!
#! @description:
#! @prerequisites:
#! @input json_input:
#! @input json_path:
#!
#!
# whatever description that will be ignored

#! @output value:
#! @output return_result:
#! @output return_code:
#! @output error_message:
#! @result SUCCESS:
#! @result FAILURE:
#!!#
#####################################################sdfdf
#!#! comment
#!

namespace: io.cloudslang.base.json

operation:
  name: get_value
  inputs:
    - json_input
    - json_path
  action:
    python_script: |
      try:
        import json
        decoded = json.loads(json_input)
        for key in json_path:
          decoded = decoded[key]
        return_code = '0'
        return_result = 'Parsing successful.'
      except Exception as ex:
        return_result = ex
        return_code = '-1'
  outputs:
    - value: ${ decoded if return_code == '0' else '' }
    - return_result
    - return_code
    - error_message: ${ return_result if return_code == '-1' else '' }
  results:
    - SUCCESS: ${ return_code == '0' }
    - FAILURE
