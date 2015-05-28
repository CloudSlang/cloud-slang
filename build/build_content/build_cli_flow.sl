#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
# Builds the cloudslang-cli.
#
# Inputs:
#   - target_dir - directory to create artifacts in - Default: target
# Results:
#   - SUCCESS
#   - FAILURE
####################################################
namespace: build.build_content

imports:
  build_content: build.build_content
  cmd: io.cloudslang.base.cmd
  files: io.cloudslang.base.files
  os: io.cloudslang.base.os
  print: io.cloudslang.base.print

flow:
  inputs:
    - include_content: false
    - target_dir: "'target'"
    - target_cli:
        default: 'target_dir + "/cloudslang-cli/cslang"'
        overridable: false
    - cloudslang_content_repo:
        default: "'https://github.com/CloudSlang/cloud-slang-content.git'"
        overridable: false
  name: build_cli_flow

  workflow:
    - copy_cloudslang_cli:
        do:
          files.copy:
            - source: "'../cloudslang-cli/target/cslang'"
            - destination: target_cli

    - copy_verifier:
        do:
          files.copy:
            - source: "'../cloudslang-content-verifier/target/cslang-builder'"
            - destination: target_dir + "/cslang-builder"

    - should_include_content:
        do:
          build_content.if:
            - expression: include_content
        navigate:
          IS: get_cloudslang_content
          IS_NOT: get_os_to_chmod

    - get_cloudslang_content:
        do:
          build_content.get_cloudslang_content:
            - url: cloudslang_content_repo
            - target_dir:
                default:  target_dir + "/cloudslang_content"
                overridable: false

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


#    - precompile_jython_standalone

    - pip_install:
        do:
          cmd.run_command:
            - command: >
                "pip install -t " + target_cli + "/python-lib " +
                "-r " + target_cli + "/python-lib/requirements.txt --compile"

    - get_os_to_chmod:
        do:
          os.get_os:
        navigate:
          LINUX: chmod_cloudslang_exec
          WINDOWS: chmod_windows_print

    - chmod_cloudslang_exec:
        do:
          cmd.run_command:
            - command: >
                "chmod +x " + target_cli + "/bin/cslang"
        navigate:
          SUCCESS: create_cli_zip
          FAILURE: FAILURE

    - chmod_windows_print:
        do:
          print.print_text:
            - text: >
                "windows os -> no chmod operation needed"


#    - add_docs


    - create_cli_zip:
        do:
          files.zip_folder:
            - archive_name: "'cslang-cli'"
            - folder_path: target_dir + "/cloudslang-cli"
            - output_folder: target_dir


    - create_builder_zip:
        do:
          files.zip_folder:
            - archive_name: "'cslang-builder'"
            - folder_path: 'target_dir + "/cslang-builder"'

#    - create_tar_gz
