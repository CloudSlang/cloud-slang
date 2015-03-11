namespace: build.build_content

imports:
  build_content: build.build_content

flow:
  name: get_slang_content
  inputs:
    - url
    - target_dir
  workflow:
    - clone_slang_content:
        do:
          build_content.clone:
            - url
            - target_location: target_dir