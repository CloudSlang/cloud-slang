#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
#!!
#! @description: Parses the given JSON input to retrieve the corresponding value addressed by the json_path input.
#! @input json_input: JSON data input - Example: '{"k1": {"k2": ["v1", "v2"]}}'
#! @input json_path: path from which to retrieve value represented as a list of keys and/or indices.
#!                   Passing an empty list ([]) will retrieve the entire json_input. - Example: ["k1", "k2", 1]
#! @output value: the corresponding value of the key referred to by json_path
#! @output return_result: parsing was successful or not
#! @output return_code: "0" if parsing was successful, "-1" otherwise
#! @output error_message: error message if there was an error when executing, empty otherwise
#! @result SUCCESS: parsing was successful (return_code == '0')
#! @result FAILURE: otherwise
#!!#
####################################################

namespace: user.ops

operation:
  name: get_value
  inputs:
    - map_input
    - map_key
  python_action:
    script: |
      import ast
      map1 = ast.literal_eval(map_input)
      try:
        for key in map_key.split(","):
          decoded = map1[key]
        return_code = '0'
        return_result = 'Parsing successful.'
      except Exception as ex:
        return_result = ex
        return_code = '-1'
  outputs:
    - value: ${ str(decoded) if return_code == '0' else '' }
    - return_result
    - return_code
    - error_message: ${ return_result if return_code == '-1' else '' }
  results:
    - SUCCESS: ${ return_code == '0' }
    - FAILURE
