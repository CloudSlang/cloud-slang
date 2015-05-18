#!/bin/bash

myrepo=cloudslang/cloud-slang
ghusertoken=$GH_TOKEN
branch=$GIT_BRANCH

cli_location=build/target/cloudslang-cli/cslang-cli.zip
builder_location=build/target/cslang-builder/cslang-builder.zip

cd $WORKSPACE


curl https://api.github.com/repos/${myrepo}/releases/tags/${branch} >> res.json

assets_url='grep -e assets_url res.json | awk \'{print $2}\' | grep -oe "[^\"].*[^\,\"]"'

echo -e "\n\nCreating release from tag branch\n=================================================================================\n"

##################################################################################################################################################

echo -e "\n\nUpoloading artifacts using curl\n=================================================================================\n"

auth_header="Authorization:token $ghusertoken"

#echo -e "\n\nUpoloading slang-cli.tar.gz\n"
#
#curl  --insecure -XPOST -H "Authorization:token $ghusertoken" -H "Content-Type:application/gzip" --data-binary @slang-cli.tar.gz https://uploads.github.com/repos/$myrepo/releases/$uniquereleaseid/assets?name=slang-cli.tar.gz

echo -e "\n\n$cli_location\n"

curl  --insecure -XPOST -H ${auth_header} -H "Content-Type:application/zip" --data-binary @${cli_location} ${assets_url}?name=cslang-cli-with-content.zip

echo -e "\n\n$builder_location\n"

curl  --insecure -XPOST -H ${auth_header} -H "Content-Type:application/zip" --data-binary @${builder_location} ${assets_url}?name=cslang-builder.zip
