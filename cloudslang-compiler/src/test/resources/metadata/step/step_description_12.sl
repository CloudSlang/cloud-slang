########################################################################################################################
#!!
#! @description: Generated flow description
#! @input input_1: Generated description flow input 1 line 1
#!
#!
#!
#!                   Generated description flow input 1 line 2
#! @input input_2# : 4$3#####*909009885^: Generated description flow input 2
#! @bo$$First: Generated description @bo$$ line 1
#!      @bo$$: Generated description @bo$$ line 1
#!                    X @bo$$: Generated description @bo$$ line 1
#!
#! @output output_1: Generated description flow output 1
#! @ output output_2: Generated description flow output 2
#! @output: Generated description flow output 3
#! @output bo$$Second: Generated description @bo$$ line 1
#!      @output bo$$: Generated description @bo$$ line 1
#!                    X @bo$$: Generated description @bo$$ line 1
#! @result SUCCESS: Flow completed successfully.
#! @result
#! @result SUCCESS
#! @result SUCCESS:
#! @result SUCCESS:
#! @nasty_tag
#! @
#! @result FAILURE: Failure occurred during execution.
#! @
#!!#
########################################################################################################################

namespace: io.cloudslang.rocks

flow:
    name: step_description_12

    inputs:
      - input_1: ""
      - input_2: ""

    workflow:
      ###################################
      #!!
      #! @description: Generated flow description
      #! @input input_1: Generated description flow input 1 line 1
      #!
      #!
      #!
      #!                   Generated description flow input 1 line 2
      #! @input input_2# : 4$3#####*909009885^: Generated description flow input 2
      #! @bo$$First: Generated description @bo$$ line 1
      #!      @bo$$: Generated description @bo$$ line 1
      #!                    X @bo$$: Generated description @bo$$ line 1
      #!
      #! @output output_1: Generated description flow output 1
      #! @ output output_2: Generated description flow output 2
      #! @output: Generated description flow output 3
      #! @output bo$$Second: Generated description @bo$$ line 1
      #!      @output bo$$: Generated description @bo$$ line 1
      #!                    X @bo$$: Generated description @bo$$ line 1
      #! @output output_SUCCESS: Flow completed successfully.
      #! @output
      #! @output output_SUCCESS
      #! @output output_SUCCESS:
      #! @output output_SUCCESS:
      #! @nasty_tag
      #! @
      #! @output output_FAILURE: Failure occurred during execution.
      #! @
      #!!#
      ##########################################################################################
      - step_1:
          do:
            operation_name:
              - step_input_1: ${input_1}
              - step_input_2: ${input_2}
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