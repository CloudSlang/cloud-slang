##What##
When we want to release a new version that:
  - Will be markes as release in Github
  - Will have artifacts that include the content (i.e. cslang-wih-content.zip)
  - Will test the content matches the language version (only default content test suite)
  - will run the CLI with a simple flow (sanity check)
  
To do so we use [drone.io] (https://drone.io/). (Contact one of the cloudslang owners for credentials)
Actually it is possible to build everything also locally, 
since all of the build scripts and flows ae available under the [/build](/build) folder of this repo.

We have there: 

1. An ant file, that runs a cslang flow, that builds and tests the CLI.  
   (we use it also in travis, but without adding and testing the content)
2. A shell script that upload the artifacts to Github, and rund sanity check.

##How##

###In Github:###

- Choose a release tag you want to build. (every release of Jenkins creates a tag)
  ![1](https://cloud.githubusercontent.com/assets/4418018/9223232/ee3cc384-4100-11e5-9cb0-84612ebc8d70.png)
 
- Copy the tag name:
  ![2](https://cloud.githubusercontent.com/assets/4418018/9223234/ee405062-4100-11e5-92b6-1006057fbf41.png)

###In drone.io:###

After you created a user and was added to the project..

-	Enter the project     

  ![3](https://cloud.githubusercontent.com/assets/4418018/9223235/ee4d7198-4100-11e5-8e28-ff73c8c8d5b3.png)

- Go to setting tab

  ![4](https://cloud.githubusercontent.com/assets/4418018/9223230/ee308c0e-4100-11e5-9e1e-131c9a037dd7.png)

- Press the little arrow next to build now

  ![5](https://cloud.githubusercontent.com/assets/4418018/9223229/ee2fbc70-4100-11e5-9554-b525db0fbc13.png)

- Press “Choose custom branch”

  ![6](https://cloud.githubusercontent.com/assets/4418018/9223231/ee33c23e-4100-11e5-8d9d-80c6ee92b6fa.png)

- Fill in the tag you want to create a release  for (you copies it from Github hopefully) in the branch name and press “build now”

 ![7](https://cloud.githubusercontent.com/assets/4418018/9223233/ee3d179e-4100-11e5-9f44-81df41b7bb00.png)

- Wait for it…..
- Look at the log (if you wish) or do something useful to the world!
