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
#   - language_codebase - path to the language codebase we are building
#                       - all other paths will be constructed relative to this path
#   - include_content - shoud include the content or not - Default: false
#   - target_dir - directory to create artifacts in - Default: target
#   - target_cli - where to put the CLI - Default: target_dir + "/cloudslang-cli/cslang"
#   - target_builder - where to put the CLI - Default: target_dir + "/cslang-builder"
#   - cloudslang_content_repo - content repo to include - Default: https://github.com/CloudSlang/cloud-slang-content.git
#   - content_branch - optional - content branch to clone
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
  name: build_cli_flow
  inputs:
    - language_codebase
    - include_content: false
    - target_dir: ${language_codebase + '/build/target'}
    - target_cli:
        default: ${target_dir + "/cloudslang-cli/cslang"}
        overridable: false
    - target_builder:
        default: ${target_dir + "/cslang-builder"}
        overridable: false
    - cloudslang_content_repo:
        default: 'https://github.com/CloudSlang/cloud-slang-content.git'
    - content_branch:
        required: false
  workflow:
    - copy_cloudslang_cli:
        do:
          files.copy:
            - source: ${language_codebase + '/cloudslang-cli/target/cslang'}
            - destination: ${target_cli}
        navigate:
          SUCCESS: copy_verifier
          FAILURE: COPY_CLI_PROBLEM

    - copy_verifier:
        do:
          files.copy:
            - source: ${language_codebase + '/cloudslang-content-verifier/target/cslang-builder'}
            - destination: ${target_builder}
        navigate:
          SUCCESS: should_include_content
          FAILURE: COPY_BUILDER_PROBLEM

    - should_include_content:
        do:
          build_content.if:
            - expression: ${include_content}
        navigate:
          IS: get_cloudslang_content
          IS_NOT: copy_changelog_to_cloudslang_cli

    - get_cloudslang_content:
        do:
          build_content.add_cloudslang_content:
            - url: ${cloudslang_content_repo}
            - content_dir: ${target_dir + "/cloudslang_content"}
            - target_dir
            - target_cli
            - target_builder
            - content_branch
        navigate:
          SUCCESS: copy_config_dir
          COMPILE_CONTENT_LINUX_PROBLEM: GET_CLOUDSLANG_CONTENT_PROBLEM
          COMPILE_CONTENT_WINDOWS_PROBLEM: GET_CLOUDSLANG_CONTENT_PROBLEM
          COPY_CONTENT_TO_CLOUDSLANG_CLI_PROBLEM: GET_CLOUDSLANG_CONTENT_PROBLEM
          COPY_PYTHON_LIB_TO_CLOUDSLANG_CLI_PROBLEM: GET_CLOUDSLANG_CONTENT_PROBLEM
          COPY_CONTENT_DOCS_TO_CLOUDSLANG_CLI_PROBLEM: GET_CLOUDSLANG_CONTENT_PROBLEM
          PIP_INSTALL_PROBLEM: GET_CLOUDSLANG_CONTENT_PROBLEM
          CLONE_CONTENT_PROBLEM: GET_CLOUDSLANG_CONTENT_PROBLEM

    - copy_config_dir:
        do:
          files.copy:
            - source: ${language_codebase + '/build/configuration'}
            - destination: ${target_cli + '/configuration'}
        navigate:
          SUCCESS: create_log4j_folder
          FAILURE: COPY_CONFIG_DIR_PROBLEM

    - create_log4j_folder:
        do:
          files.create_folder:
            - folder_name: ${target_cli + '/configuration/logging/'}
        navigate:
          SUCCESS: copy_log4j_file
          FAILURE: CREATE_LOG4J_FOLDER_PROBLEM

    - copy_log4j_file:
        do:
          files.copy:
            - source: ${language_codebase + '/cloudslang-cli/src/main/resources/log4j.properties'}
            - destination: ${target_cli + '/configuration/logging/log4j.properties'}
        navigate:
          SUCCESS: copy_changelog_to_cloudslang_cli
          FAILURE: COPY_LOG4J_FILE_PROBLEM

    - copy_changelog_to_cloudslang_cli:
        do:
          files.copy:
            - source: ${language_codebase + '/CHANGELOG.md'}
            - destination: ${target_cli + '/CHANGELOG.md'}
        navigate:
          SUCCESS: get_os_to_chmod
          FAILURE: COPY_CHANGELOG_TO_CLOUDSLANG_CLI_PROBLEM

    - get_os_to_chmod:
        do:
          os.get_os:
        navigate:
          LINUX: chmod_cloudslang_exec
          WINDOWS: chmod_windows_print

    - chmod_cloudslang_exec:
        do:
          cmd.run_command:
            - command: ${"chmod +x " + target_cli + "/bin/cslang"}
        navigate:
          SUCCESS: create_cli_zip
          FAILURE: CHMOD_CLOUDSLANG_EXEC_FAILURE

    - chmod_windows_print:
        do:
          print.print_text:
            - text: 'windows os -> no chmod operation needed'
        navigate:
          SUCCESS: create_cli_zip

    - create_cli_zip:
        do:
          files.zip_folder:
            - archive_name: 'cslang-cli'
            - folder_path: ${target_dir + "/cloudslang-cli"}
        navigate:
          SUCCESS: create_cli_tar_gz
          FAILURE: CREATE_CLI_ZIP_PROBLEM

    - create_cli_tar_gz:
        do:
          cmd.run_command:
            - command: ${"cd " + target_dir + "/cloudslang-cli && tar -cvzf cslang-cli.tar.gz cslang"}
        navigate:
          SUCCESS: should_create_builder_zip
          FAILURE: CREATE_CLI_TAR_GZ_PROBLEM

    # if content was added we should not generate builder zip (it was used previously, it is not cleaned up)
    - should_create_builder_zip:
        do:
          build_content.if:
            - expression: ${include_content}
        navigate:
          IS: SUCCESS
          IS_NOT: create_builder_zip

    - create_builder_zip:
        do:
          files.zip_folder:
            - archive_name: 'cslang-builder'
            - folder_path: ${target_dir + "/cslang-builder"}
        navigate:
          SUCCESS: SUCCESS
          FAILURE: CREATE_BUILDER_ZIP_PROBLEM
  results:
    - SUCCESS
    - COPY_CLI_PROBLEM
    - COPY_BUILDER_PROBLEM
    - GET_CLOUDSLANG_CONTENT_PROBLEM
    - COPY_CONFIG_DIR_PROBLEM
    - CREATE_LOG4J_FOLDER_PROBLEM
    - COPY_LOG4J_FILE_PROBLEM
    - COPY_CHANGELOG_TO_CLOUDSLANG_CLI_PROBLEM
    - CHMOD_CLOUDSLANG_EXEC_FAILURE
    - CREATE_CLI_ZIP_PROBLEM
    - CREATE_CLI_TAR_GZ_PROBLEM
    - CREATE_BUILDER_ZIP_PROBLEM
