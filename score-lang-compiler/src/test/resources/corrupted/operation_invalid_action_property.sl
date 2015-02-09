namespace: user.ops

operation:
  name: operation_invalid_action_property
  action:
    java_action:
      className: com.hp.thing
      methodName: someMethod
      IDontBelongHere: blah
