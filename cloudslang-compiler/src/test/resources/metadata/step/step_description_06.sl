#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
########################################################################################################################
#!!
#! @description: Generated flow description
#! @prerequisites: Generated flow prerequisites
#!
#! @input input_1:
    #!                   Example: '{"k1": {"k2": ["v1", "v2"]}}'
#! @input input_2: Generated description flow input 2
#!
  #! @output output_1: Generated description flow output 1
#! @output return_code: '0' if parsing was successful, '-1' otherwise
#!
#!!#
########################################################################################################################
#!#! comment
#!

namespace: io.cloudslang.rocks

flow:
    name: step_description_06

    inputs:
      - input_1: ""
      - input_2: ""

    workflow:
      - step_6:
          do:
            operation_name:
              - step_input_1: ${input_1}
              - step_input_4: ${input_2}
          publish:
            - step_output_1
            - step_output_2
          navigate:
            - SUCCESS: SUCCESS
            - FAILURE: FAILURE
    outputs:
      - output_1: ${step_output_1}

    results:
      - SUCCESS
      - FAILURE
########################################################################################################################
#!!
#! @description: Generated flow description second
#! @prerequisites: Generated flow prerequisites
#!
#! @input input_1:
    #!                   Example: '{"k1": {"k2": ["v1", "v2"]}}'
#! @input input_2: Generated description flow input 2
#!
  #! @output output_1: Generated description flow output 1
#! @output return_code: '0' if parsing was successful, '-1' otherwise
#!
#!!#
########################################################################################################################