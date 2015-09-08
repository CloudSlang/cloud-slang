##What
When we want to release a new version that:

  - Will be marked as release in GitHub
  - Will have artifacts that include the content (i.e. cslang-with-content.zip)
  - Will test that the content matches the language version (only the default content test suite)
  - Will run the CLI with a simple flow (sanity check)
  
To do so we use [drone.io] (https://drone.io/). (Contact one of the cloudslang owners for credentials.)

It is also possible to build everything locally since all of the build scripts and flows are available under the [/build](/build) folder of this repo.

In the build folder we have: 

- An Ant file, that runs a CloudSlang flow, that builds and tests the CLI (we also use it in Travis, but without adding and testing the content).
- A shell script that uploads the artifacts to GitHub, and runs sanity checks.

##How

###In GitHub:

- Choose a release tag you want to build. (Every release of Jenkins creates a tag.)
  ![1](https://cloud.githubusercontent.com/assets/4418018/9223232/ee3cc384-4100-11e5-9cb0-84612ebc8d70.png)
 
- Copy the tag name.
  ![2](https://cloud.githubusercontent.com/assets/4418018/9223234/ee405062-4100-11e5-92b6-1006057fbf41.png)

###In drone.io:

After you created a user and was added to the project:

-	Enter the project.

  ![3](https://cloud.githubusercontent.com/assets/4418018/9223235/ee4d7198-4100-11e5-8e28-ff73c8c8d5b3.png)

- Go to the **Settings** tab.

  ![4](https://cloud.githubusercontent.com/assets/4418018/9223230/ee308c0e-4100-11e5-9e1e-131c9a037dd7.png)

- Click the arrow next to **Build Now**.

  ![5](https://cloud.githubusercontent.com/assets/4418018/9223229/ee2fbc70-4100-11e5-9554-b525db0fbc13.png)

- Click **Choose Custom Branch**.

  ![6](https://cloud.githubusercontent.com/assets/4418018/9223231/ee33c23e-4100-11e5-8d9d-80c6ee92b6fa.png)

- Enter the tag you want to create a release for (the one copied from GitHub) in the **Branch Name** and press **Build now**.

 ![7](https://cloud.githubusercontent.com/assets/4418018/9223233/ee3d179e-4100-11e5-9f44-81df41b7bb00.png)

- Check the log (if you wish).
