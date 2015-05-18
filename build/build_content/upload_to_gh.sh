#!/bin/bash

myrepo=cloudslang/cloud-slang
ghusertoken=$GH_TOKEN
branch=$GIT_BRANCH
build_dir=$WORKSPACE

cli_location=build/target/cloudslang-cli/cslang-cli.zip
builder_location=build/target/cslang-builder/cslang-builder.zip

cd ${build_dir}

echo -e "fetching https://api.github.com/repos/${myrepo}/releases/tags/${branch}\n"

curl https://api.github.com/repos/${myrepo}/releases/tags/${branch} > ${build_dir}/res.json

assets_url=$(grep -e assets_url ${build_dir}/res.json | awk '{print $2}' | grep -oe '[^\"].*[^\,\"]')

echo ${assets_url}


##################################################################################################################################################

echo -e "\n\nUpoloading artifacts using curl\n=================================================================================\n"

#echo -e "\n\nUpoloading slang-cli.tar.gz\n"
#
#curl  --insecure -XPOST -H "Authorization:token $ghusertoken" -H "Content-Type:application/gzip" --data-binary @slang-cli.tar.gz https://uploads.github.com/repos/$myrepo/releases/$uniquereleaseid/assets?name=slang-cli.tar.gz

echo -e "\n\n$cli_location\n"

curl  --insecure -XPOST -H "Authorization:token ${ghusertoken}" -H "Content-Type:application/zip" --data-binary @${cli_location} ${assets_url}?name=cslang-cli-with-content.zip

#echo -e "\n\n$builder_location\n"

#curl  --insecure -XPOST -H "Authorization:token $ghusertoken" -H "Content-Type:application/zip" --data-binary @${builder_location} ${assets_url}?name=cslang-builder.zip

