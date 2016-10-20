#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0

namespace: user.ops

operation:
  name: compute_daylight_time_zone
  inputs:
    - time_zone_as_string
  python_action:
    script: |
      daylight_time_zone = int(time_zone_as_string) + 1
      print 'daylight time zone is: ' + str(daylight_time_zone)
  outputs:
    - daylight_time_zone: ${ str(daylight_time_zone) }
  results:
    - SUCCESS: ${ 1 == 1 }
    - FAILURE
