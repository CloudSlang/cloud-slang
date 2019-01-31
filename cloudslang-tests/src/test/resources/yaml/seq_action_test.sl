#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: seq_action_test
  inputs:
    - host
    - port
    - param1:
        default: "le_def_val"
  sequential_action:
    gav: 'cloudslang.seq.recordings:register_user:1.0'
    steps:
            - step:
                id: '1'
                object_path: Window("Notepad").WinEditor("Edit")
                action: SetCaretPos
                args: Parameter("param1")
                snapshot: .\Snapshots\ssf1.png
                highlight_id: '198258'
  results:
    - SUCCESS
    - WARNING
    - FAILURE