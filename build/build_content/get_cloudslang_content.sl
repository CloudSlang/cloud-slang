#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
# Gets the cloud-slang-content repository.
#
# Inputs:
#   - url - URL of content repository
#   - target_dir - location to put content repository in
# Results:
#   - SUCCESS
#   - FAILURE
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