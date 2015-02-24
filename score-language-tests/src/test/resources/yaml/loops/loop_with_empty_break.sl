namespace: loops

imports:
  ops: loops

flow:
  name: loop_with_empty_break
  inputs:
    - values: [1,2,3]
  workflow:
    print_values:
      loop:
        for: value in range(0, 3)
        do:
          ops.operation_that_fails_when_value_is_2:
            - text: value
        break: []
