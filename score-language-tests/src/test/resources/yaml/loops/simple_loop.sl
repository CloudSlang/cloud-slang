namespace: loops

imports:
  ops: loops

flow:
  name: simple_loop
  inputs:
    - values: [1,2,3]
  workflow:
    - print_values:
        loop:
          for: value in values
          do:
            ops.print:
              - text: value