namespace: io.content

imports:
  ops: io.content

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
    - output: inexistend_output
  # results:


