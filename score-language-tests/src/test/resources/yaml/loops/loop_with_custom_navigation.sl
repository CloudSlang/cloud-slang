namespace: loops

imports:
  ops: loops

flow:
  name: loop_with_custom_navigation
  inputs:
    - values: [1,2]
  workflow:
    print_values:
      loop:
        for: value in values
        do:
          ops.print:
            - text: value
      navigate:
        SUCCESS: print_other_values

    task_that_doesnt_run:
      do:
        ops.print:
          - text: "'I don't run'"

    print_other_values:
      do:
        ops.print:
          - text: "'abc'"