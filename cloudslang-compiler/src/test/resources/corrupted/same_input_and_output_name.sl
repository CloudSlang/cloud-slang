#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
#####################################################

namespace: io.cloudslang.base.json

operation:
  name: same_input_and_output_name
  inputs:
    - json_input
    - json_path
  python_action:
      script: |
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
    - json_path
    - error_message: ${ return_result if return_code == '-1' else '' }
  results:
    - SUCCESS: ${ return_code == '0' }
    - FAILURE
