#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
#
#   this operation clones a remote repository to a specified location
#
#    Inputs:
#      - url - URL to of the remote repository to clone
#      - target_location - location of the cloned repository
####################################################

namespace: build.build_content

imports:
  cmd: org.openscore.slang.base.cmd

flow:
  name: clone
  inputs:
    - url
    - target_location: "'.'"
  workflow:
    - clone:
        do:
          cmd.run_command:
            - command: >
                'git clone ' + url + ' ' + target_location
