namespace: user.ops

operation:
  name: external_python_action_simple
  python_action:
    useJython: false
    script: "def execution():\n    a = 2 \n    b= \"ceva\" \nreturn {\"a\": 2}"

  results:
    - SUCCESS
