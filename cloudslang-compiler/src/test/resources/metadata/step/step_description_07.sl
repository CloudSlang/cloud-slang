#!!
#! @description: Generated flow description
#! @prerequisites: Generated flow prerequisites
#!
#! @input input_1: Generated description flow input 1 line 1
#!                   Generated description flow input 1 line 2

#! @input input_2: Generated description flow input 2
#!
# @output output_1: Generated description flow output 1
#!
#! @result SUCCESS: Flow completed successfully.
#! @result FAILURE: Failure occurred during execution.
#!!#
########################################################################################################################hfghgfhfgh

namespace: io.cloudslang.rocks

flow:
    name: step_description_05

    inputs:
      - input_1: ""
      - input_2: ""

    workflow:
      #!!
      #! @input step_input_1: description step input 1
      #! @input step_input_2 description step input 2 line 1
      #!                        description step input 2 line 2
      #! @input step_input_3:
      #! @output step_output_1: description step output 11
      #! @output step_output_1: description step output 12
      #! @output step_output_2 : description step output 2
      #!!#
      - step_1:
          do:
            operation_name:
              - step_input_1: ${input_1}
              - step_input_2: ${input_2}
          publish:
            - step_output_1
            - step_output_2
          navigate:
            - SUCCESS: step_2
            - FAILURE: FAILURE

      - step_2:
          do:
            operation_name:
              - step_input_1: ${input_1}
              - step_input_2: ${input_2}
          publish:
            - step_output_1
            - step_output_2
          navigate:
            - SUCCESS: step_3
            - FAILURE: FAILURE

      - step_3:
          do:
            operation_name:
              - step_input_1: ${input_1}
              - step_input_4: ${input_2}
          publish:
            - step_output_1
            - step_output_2
          navigate:
            - SUCCESS: step_4
            - FAILURE: FAILURE

      - step_4:
          do:
            operation_name:
              - step_input_1: ${input_1}
              - step_input_4: ${input_2}
          publish:
            - step_output_1
            - step_output_2
          navigate:
            - SUCCESS: step_5
            - FAILURE: FAILURE

      - step_5:
          do:
            operation_name:
              - step_input_1: ${input_1}
              - step_input_4: ${input_2}
          publish:
            - step_output_1
            - step_output_2
          navigate:
            - SUCCESS: step_6
            - FAILURE: FAILURE

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