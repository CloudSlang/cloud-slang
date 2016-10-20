namespace: slang-deployment-cp-1.folder1.folder2

operation:
  name: op1
  inputs:
    - text
  python_action:
    script: |
      returnResult="Wow man"
      print text
  outputs:
    - returnResult
  results:
    - SUCCESS