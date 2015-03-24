namespace: user.ops

operation:
  name: test_op
  action:
    python_script: 'print "hello world"'
  outputs:
    - weather: "'stam'"
