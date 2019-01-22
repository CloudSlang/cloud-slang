namespace: rpaf
operation:
  name: simple_valid_rpa_op
  rpa_action:
    gav: rpa:rpaf.simple_valid_rpa_op:1.0.0
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
