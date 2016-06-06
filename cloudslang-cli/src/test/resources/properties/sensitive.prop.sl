#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.sys

properties:
  props.var1_non_sensitive: 'value var1_non_sensitive'
  props.var2_non_sensitive:
    value: 'value var2_non_sensitive'
    sensitive: false
  props.var3_sensitive:
    value: 'value var3_sensitive'
    sensitive: true
