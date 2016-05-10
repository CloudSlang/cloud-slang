#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
##################################################
# Verifies two strings are equal.
#
# Inputs:
#   - first_string - first string to compare
#   - second_string - second string to compare
# Results:
#   - SUCCESS - if strings are equal
#   - FAILURE - otherwise
##################################################
namespace: user.ops

operation:
  name: string_equals
  inputs:
    - first_string
    - second_string
  python_action:
    script: |
      res = False
      if first_string == second_string:
        res = True
  results:
    - SUCCESS: ${ res == True }
    - FAILURE
