namespace: base

imports:
  ops: base

flow:
  name: test_print_text-EXCEPTION
  inputs:
    - text
  workflow:
    - test_print_text_simple:
        do:
          ops.print_text:
            - text
        # publish:
        # navigate:
  outputs:
    - output: ${ inexistent_output }
  # results:
