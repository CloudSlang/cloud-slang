#   (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Apache License v2.0 which accompany this distribution.
#
#   The Apache License is available at
#   http://www.apache.org/licenses/LICENSE-2.0
#
####################################################
#
#   this flow builds the cloudslang-cli
#
#    Inputs:
#      - target_dir - the directory to create the artifacts in
####################################################
namespace: build.build_content

imports:
  build_content: build.build_content
  cmd: io.cloudslang.base.cmd
  files: io.cloudslang.base.files

flow:
  inputs:
    - target_dir: "'target'"
    - target_cli:
        default: 'target_dir + "/cloudslang-cli/cslang"'
        overridable: false
    - cloudslang_content_repo:
        default: "'https://github.com/CloudSlang/cloud-slang-content.git'"
        overridable: false
  name: build_cli_flow

  workflow:
    - create_target_dir:
        do:
          files.create_folder:
            - folder_name: target_dir

    - create_target_cli_dir:
        do:
          cmd.run_command:
            - command: >
                "mkdir target/cloudslang-cli && mkdir target/cloudslang-cli/cslang"


    - get_cloudslang_content:
        do:
          build_content.get_cloudslang_content:
            - url: cloudslang_content_repo
            - target_dir:
                default:  target_dir + "/cloudslang_content"
                overridable: false

    - copy_verifier:
            do:
              cmd.run_command:
                - command: >
                    "cp -r  ../cloudslang-content-verifier/target/cslang-builder " + target_dir

    - run_verifier:
        do:
          cmd.run_command:
            - command: >
                "bash " +
                target_dir + "/cslang-builder/bin/cslang-builder " +
                target_dir + "/cloudslang_content/content/"

    - copy_cloudslang_cli:
        do:
          cmd.run_command:
            - command: >
                "cp -r ../cloudslang-cli/target/cslang " + target_cli

    - copy_content_to_cloudslang_cli:
        do:
          files.copy:
            - source: target_dir + '/cloudslang_content/content'
            - destination: target_cli + '/content'

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

    - chmod_cloudslang_exec:
        do:
          cmd.run_command:
            - command: >
                "chmod +x " + target_cli + "/cslang/bin/cslang"

#    - add_docs

    - create_cli_zip:
        do:
          files.zip_folder:
            - archive_name: "'cloudslang-cli'"
            - folder_path: 'target_dir + "/cloudslang-cli"'
            - output_folder: target_dir


    - create_builder_zip:
        do:
          files.zip_folder:
            - archive_name: "'cloudslang-builder'"
            - folder_path: 'target_dir + "/cslang-builder"'

#    - create_tar_gz
