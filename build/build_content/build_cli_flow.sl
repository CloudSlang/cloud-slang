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
#   - include_content - shoud include the content or not - Default: false
#   - target_dir - directory to create artifacts in - Default: target
#   - target_cli - where to put the CLI - Default: target_dir + "/cloudslang-cli/cslang"
#   - cloudslang_content_repo - content repo to include = Default: https://github.com/CloudSlang/cloud-slang-content.git
#   - content_branch - content branch to clone
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
    - content_branch:
        required: false
        
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
          IS_NOT: copy_changelog_to_cloudslang_cli


    # adding content


    - get_cloudslang_content:
        do:
          build_content.add_cloudslang_content:
            - url: cloudslang_content_repo
            - content_dir: target_dir + "/cloudslang_content"
            - target_dir
            - target_cli
            - content_branch


    #    end adding content


    - copy_changelog_to_cloudslang_cli:
        do:
          files.copy:
            - source: "'../CHANGELOG.md'"
            - destination: target_cli + '/CHANGELOG.md'

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

    - create_cli_tar_gz:
            do:
              cmd.run_command:
                - command: >
                    "tar -cvzf " + target_dir + "/cslang-cli.tar.gz " + target_dir + "/cloudslang-cli"

    - create_builder_zip:
        do:
          files.zip_folder:
            - archive_name: "'cslang-builder'"
            - folder_path: 'target_dir + "/cslang-builder"'
