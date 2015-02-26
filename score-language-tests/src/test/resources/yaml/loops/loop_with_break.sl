namespace: loops

imports:
  ops: loops

flow:
  name: loop_with_break
  inputs:
    - values: [1,2,3]
  workflow:
    - print_values:
        loop:
          for: value in values
          do:
            ops.operation_that_goes_to_custom_when_value_is_2:
              - text: value
          break:
            - CUSTOM
        navigate:
          CUSTOM: print_other_values
          SUCCESS: task_that_doesnt_run

    - task_that_doesnt_run:
        do:
          ops.print:
            - text: "'I don't run'"

    - print_other_values:
        do:
          ops.print:
            - text: "'abc'"