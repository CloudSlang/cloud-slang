#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
# Prints sys property to the screen.
#
# Inputs:
#   - text - text to print
# Results:
#   - SUCCESS
####################################################

namespace: base

operation:
  name: print_property
  inputs:
    - text:
        system_property: base.prop1
  action:
    python_script: print text
  results:
    - SUCCESS: ${ text != "" }