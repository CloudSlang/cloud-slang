#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: a.b

properties:
  - op.var1: 'value op var1'
  - op.var2:
      value: 'value op var2'
      sensitive: false
  - op.var3_sensitive:
      value: 'value op var3_sensitive'
      sensitive: true
  - flow.var1: 'value flow var1'
  - flow.var2:
      value: 'value flow var2'
      sensitive: false
  - flow.var3_sensitive:
      value: 'value flow var3_sensitive'
      sensitive: true
