namespace: loops

operation:
  name: operation_that_goes_to_custom_when_value_is_2
  inputs: ['text']
  action:
    python_script: print text
  results:
    - CUSTOM: fromInputs['text'] == 2
    - SUCCESS