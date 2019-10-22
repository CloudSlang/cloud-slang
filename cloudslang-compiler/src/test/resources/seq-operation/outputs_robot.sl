namespace: namespace
operation:
  name: outputs_robot
  outputs:
    - output1:
        value: abc
        robot: true
    - output2:
        value: '123'
        robot: false
    - output3
  sequential_action:
    gav: com.microfocus.seq:namespace.name:1.0.0
    steps:
      - step:
          id: '1'
          object_path: Window("Notepad").WinEditor("Edi"t"\.")
          action: Type
          args: Parameter("Param1\Param2")
          default_args: Parameter("Param1\Param2")
  results:
    - SUCCESS
    - WARNING
    - FAILURE
