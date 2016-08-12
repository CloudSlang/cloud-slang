#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: get_time_zone
  inputs:
    - time_zone_as_string
    - alla: ${get_sp('user.sys.props.alla')}
  python_action:
    script: |
      time_zone_as_int = int(time_zone_as_string)
      print 'time zone is: ' + str(time_zone_as_int)
  outputs:
    - time_zone: ${ str(time_zone_as_int) }
  results:
    - NEGATIVE: ${ int(time_zone_as_int) < 0 }
    - SUCCESS