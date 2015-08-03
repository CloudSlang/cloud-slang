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
#   - content_dir - location to put content repository in
#   - target_dir - location of the target/build dir
#   - content_branch - Optional - content branch to checkout
#   - target_cli - location of the CLI we're building
# Results:
#   - SUCCESS
#   - FAILURE
####################################################
namespace: build.build_content

imports:
  build_content: build.build_content
  files: io.cloudslang.base.files
  os: io.cloudslang.base.os
  cmd: io.cloudslang.base.cmd


flow:
  name: add_cloudslang_content
  inputs:
    - url
    - content_dir
    - target_dir
    - target_cli
    - content_branch:
        required: false
  workflow:
    - clone_slang_content:
        do:
          build_content.clone:
            - url
            - target_location: content_dir
            - branch:
                default: content_branch
                required: false

    - get_os_to_verify:
        do:
          os.get_os:
        navigate:
          LINUX: run_verifier_linux
          WINDOWS: run_verifier_windows

    - run_verifier_linux:
        do:
          cmd.run_command:
            - command: >
                "bash " +
                target_dir + "/cslang-builder/bin/cslang-builder " +
                target_dir + "/cloudslang_content"
        navigate:
          SUCCESS: copy_content_to_cloudslang_cli
          FAILURE: FAILURE

    - run_verifier_windows:
        do:
          cmd.run_command:
            - command: >
                target_dir + "\\cslang-builder\\bin\\cslang-builder.bat " +
                target_dir + "/cloudslang_content"

    - copy_content_to_cloudslang_cli:
        do:
          files.copy:
            - source: target_dir + '/cloudslang_content/content'
            - destination: target_cli + "/content"

    - copy_python_lib_to_cloudslang_cli:
        do:
          files.copy:
            - source: target_dir + '/cloudslang_content/python-lib'
            - destination: target_cli + '/python-lib'

    - copy_content_docs_to_cloudslang_cli:
        do:
          files.copy:
            - source: target_dir + '/cloudslang_content/DOCS.md'
            - destination: target_cli + '/DOCS.md'

    - pip_install:
        do:
          cmd.run_command:
            - command: >
                "pip install -t " + target_cli + "/python-lib " +
                "-r " + target_cli + "/python-lib/requirements.txt --compile"