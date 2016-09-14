#!!
#! @description: Kuku <b>muku</b> puku
#!!#
namespace: slang-deployment-cp-1.folder1.folder2
imports:
  ops: slang-deployment-cp-1.folder1.folder2

flow:
  inputs:
    - text:
        required: False
  name: flow1
  workflow:
    - sayHi:
        do:
          ops.op1:
            - text
        publish:
          - answer: returnResult
  outputs:
    - returnResult: answer
