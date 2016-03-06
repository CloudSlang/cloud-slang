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
#   - target_dir - location of the parent target dir
#   - target_cli - location of the CLI we're building
#   - target_builder - location of the builder we're building
#   - content_branch - optional - content branch to checkout
# Results:
#    - SUCCESS
#    - COMPILE_CONTENT_LINUX_PROBLEM
#    - COMPILE_CONTENT_WINDOWS_PROBLEM
#    - COPY_CONTENT_TO_CLOUDSLANG_CLI_PROBLEM
#    - COPY_PYTHON_LIB_TO_CLOUDSLANG_CLI_PROBLEM
#    - COPY_CONTENT_DOCS_TO_CLOUDSLANG_CLI_PROBLEM
#    - PIP_INSTALL_PROBLEM
#    - CLONE_CONTENT_PROBLEM
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
    - target_builder
    - content_branch:
        required: false
  workflow:
    - clone_content:
        do:
          build_content.clone:
            - url
            - target_location: ${content_dir}
            - branch: ${content_branch}
        navigate:
          SUCCESS: get_os_to_verify
          FAILURE: CLONE_CONTENT_PROBLEM

    - get_os_to_verify:
        do:
          os.get_os:
        navigate:
          LINUX: compile_content_linux
          WINDOWS: compile_content_windows

    - compile_content_linux:
        do:
          cmd.run_command:
            - command: >
                ${"bash " +
                target_builder + "/bin/cslang-builder " +
                target_dir + "/cloudslang_content " +
                "-ts !default"}
        navigate:
          SUCCESS: copy_content_to_cloudslang_cli
          FAILURE: COMPILE_CONTENT_LINUX_PROBLEM

    - compile_content_windows:
        do:
          cmd.run_command:
            - command: >
                ${target_builder + "\\bin\\cslang-builder.bat " +
                target_dir + "/cloudslang_content " +
                "-ts !default"}
        navigate:
          SUCCESS: copy_content_to_cloudslang_cli
          FAILURE: COMPILE_CONTENT_WINDOWS_PROBLEM

    - copy_content_to_cloudslang_cli:
        do:
          files.copy:
            - source: ${target_dir + '/cloudslang_content/content'}
            - destination: ${target_cli + "/content"}
        navigate:
          SUCCESS: copy_python_lib_to_cloudslang_cli
          FAILURE: COPY_CONTENT_TO_CLOUDSLANG_CLI_PROBLEM

    - copy_python_lib_to_cloudslang_cli:
        do:
          files.copy:
            - source: ${target_dir + '/cloudslang_content/python-lib'}
            - destination: ${target_cli + '/python-lib'}
        navigate:
          SUCCESS: copy_content_docs_to_cloudslang_cli
          FAILURE: COPY_PYTHON_LIB_TO_CLOUDSLANG_CLI_PROBLEM

    - copy_content_docs_to_cloudslang_cli:
        do:
          files.copy:
            - source: ${target_dir + '/cloudslang_content/DOCS.md'}
            - destination: ${target_cli + '/DOCS.md'}
        navigate:
          SUCCESS: pip_install
          FAILURE: COPY_CONTENT_DOCS_TO_CLOUDSLANG_CLI_PROBLEM

    - pip_install:
        do:
          cmd.run_command:
            - command: >
                ${"pip install -t " + target_cli + "/python-lib " +
                "-r " + target_cli + "/python-lib/requirements.txt --compile"}
        navigate:
          SUCCESS: SUCCESS
          FAILURE: PIP_INSTALL_PROBLEM
  results:
    - SUCCESS
    - COMPILE_CONTENT_LINUX_PROBLEM
    - COMPILE_CONTENT_WINDOWS_PROBLEM
    - COPY_CONTENT_TO_CLOUDSLANG_CLI_PROBLEM
    - COPY_PYTHON_LIB_TO_CLOUDSLANG_CLI_PROBLEM
    - COPY_CONTENT_DOCS_TO_CLOUDSLANG_CLI_PROBLEM
    - PIP_INSTALL_PROBLEM
    - CLONE_CONTENT_PROBLEM
