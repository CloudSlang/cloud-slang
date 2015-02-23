namespace: loops

operation:
  name: operation_that_fails_when_value_is_2
  inputs: ['text']
  action:
    python_script: print text
  results:
    - FAILURE: fromInputs['text'] == 2
    - SUCCESS