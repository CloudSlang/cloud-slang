namespace: seqf
operation:
  name: simple_valid_seq_op
  sequential_action:
    gav: seq:seqf.simple_valid_seq_op:1.0.0
    steps:
    - step:
        id: '1'
        object_path: Browser("Browser")
        action: Navigate
        args: '"https://www.google.com/"'
        snapshot: .\Snapshots\ssf2.html
        highlight_id: '12345'
  results:
  - SUCCESS
  - WARNING
  - FAILURE
