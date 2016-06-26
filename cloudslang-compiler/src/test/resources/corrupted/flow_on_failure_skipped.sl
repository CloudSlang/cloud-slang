#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
namespace: test_code.junk

flow:
  name: flow_on_failure_skipped

  inputs:
    - value: 'fail'

  workflow:
    - switcher:
        do:
           operation:
             - to_test: ${value}
    - on_failure:
      -print_fail:
          do:
            print:
              - text: "failed miserably"