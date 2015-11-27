#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
# IF: utility operation
#
# Inputs:
#   - expression - expression to test
# Results:
#   - IS: expression is true
#   - IS_NOT: else
####################################################

namespace: build.build_content

operation:
  name: if
  inputs:
    - expression
  action:
    python_script: expression
  results:
    - IS: >
        ${expression in [True, 'True', 'true', 1, '1', 't', 'y', 'yes',
        'yeah', 'yup', 'certainly', 'uh-huh', 'tots', 'positive']}
    - IS_NOT
