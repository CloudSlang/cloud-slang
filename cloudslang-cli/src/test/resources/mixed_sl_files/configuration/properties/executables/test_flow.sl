namespace: user.ops

imports:
  ops: user.ops

flow:
  name: basic_flow
  inputs:
    - input1
  workflow:
    - CheckWeather:
        do:
          ops.test_op:
            - city: input1
        publish:
          - weather
        navigate:
          SUCCESS: SUCCESS
          FAILURE: FAILURE
  results:
    - SUCCESS
    - FAILURE