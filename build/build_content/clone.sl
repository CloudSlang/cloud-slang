#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
# Clones a remote git repository to a specified location.
#
# Inputs:
#   - url - URL of the remote repository to clone
#   - target_location - location of the cloned repository - Default: .
#   - branch - branch to checkout - Default: master
####################################################

namespace: build.build_content

imports:
  cmd: io.cloudslang.base.cmd

flow:
  name: clone
  inputs:
    - url
    - target_location: '.'
    - branch: 'master'
  workflow:
    - clone:
        do:
          cmd.run_command:
            - command: >
                ${'git clone -b ' + branch + ' ' + url + ' ' + target_location}
