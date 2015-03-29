#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
#
#   this flow gets the slang-content repo
#
#    Inputs:
#      - url - URL to of the content repo
#      - target_dir - location to puth the content repo in
####################################################
namespace: build.build_content

imports:
  build_content: build.build_content

flow:
  name: get_cloudslang_content
  inputs:
    - url
    - target_dir
  workflow:
    - clone_slang_content:
        do:
          build_content.clone:
            - url
            - target_location: target_dir