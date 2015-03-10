namespace: build.sub_flows

imports:
  git: org.openscore.slang.git

flow:
  name: get_slang_content
  inputs:
    - url
    - target_dir
  workflow:
    - clone_slang_content:
        do:
          git.clone:
            - url
            - target_location: target_dir