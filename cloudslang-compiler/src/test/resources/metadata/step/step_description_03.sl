namespace: io.cloudslang.rocks

flow:
    name: step_description_03

    inputs:
      - input_1: ""
      - input_2: ""

    workflow:
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