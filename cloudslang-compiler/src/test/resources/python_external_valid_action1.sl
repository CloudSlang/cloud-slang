namespace: user.ops

operation:
  name: external_python_action_simple
  python_action:
    useJython: false
    script: "def execution(a, b, c):\n    a = 2 \n    b= \"ceva\""

  results:
    - SUCCESS
