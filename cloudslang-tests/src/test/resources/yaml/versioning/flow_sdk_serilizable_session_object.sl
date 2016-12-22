#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: io.cloudslang.flows

imports:
  ops: io.cloudslang.ops
  nop: user.ops

flow:
  name: flow_sdk_serilizable_session_object

  workflow:
    # 1: first call to problematic action
    - serializable_session_1:
        do:
          ops.op_sdk_serilizable_session_object: []
        navigate:
          - SUCCESS: noops

    # 2: parallel_loop
    - noops:
        parallel_loop:
          for: n in ('0','1','2')
          do:
            nop.noop: []
        navigate:
          - SUCCESS: serializable_session_2

    # 3: second call to problematic action
    - serializable_session_2:
        do:
          ops.op_sdk_serilizable_session_object: []
        navigate:
          - SUCCESS: SUCCESS
  results:
    - SUCCESS
