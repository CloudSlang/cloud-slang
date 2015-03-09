namespace: build

flow:
  name: build_cli_flow
  workflow:
    - clone_slang_content
    - run_verifier
    - copy_slang_cli
#    - precompile_jython_standalone
    - pip_install
    - chmod_slang_exec
#    - add_docs
    - create_zip
    - create_tar_gz