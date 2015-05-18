#!/bin/bash

REPO=cloudslang/cloud-slang
GHUSERTOKEN=$GH_TOKEN
BRANCH=$GIT_BRANCH
BUILD_DIR=$WORKSPACE

CLI_DIR=build/target/cloudslang-cli
CLI_PATH=${CLI_DIR}/cslang-cli.zip
BUILDER_PATH=build/target/cslang-builder/cslang-builder.zip

cd ${BUILD_DIR}

echo -e "fetching https://api.github.com/repos/${REPO}/releases/tags/${BRANCH}\n"

curl https://api.github.com/repos/${REPO}/releases/tags/${BRANCH} > ${BUILD_DIR}/res.json


RELEASEID=$(grep -e id ${BUILD_DIR}/res.json | head -n 1 | awk '{print $2}' | grep -oe '[0-9]*')
UPLOAD_URL="https://uploads.github.com/repos/$REPO/releases/$RELEASEID/assets"

echo ${UPLOAD_URL}


##################################################################################################################################################

echo -e "\n\nUpoloading artifacts using curl\n=================================================================================\n"

#echo -e "\n\nUpoloading slang-cli.tar.gz\n"
#
#curl  --insecure -XPOST -H "Authorization:token $GHUSERTOKEN" -H "Content-Type:application/gzip" --data-binary @slang-cli.tar.gz https://uploads.github.com/repos/$REPO/releases/$uniquereleaseid/assets?name=slang-cli.tar.gz

echo -e "\n\n$CLI_PATH\n"

bash $CLI_DIR/cslang/bin/cslang run --f $CLI_DIR/cslang/content/io/cloudslang/base/print/print_text.sl --i text=hi

FILESIZE=`stat -c '%s' "$CLI_PATH"`
curl -s -H "Authorization:token ${GHUSERTOKEN}" -H "Content-Type:application/zip" --data-binary "@${CLI_PATH}" "${UPLOAD_URL}?name=cslang-cli-with-content.zip&size=$FILESIZE"

#echo -e "\n\n$BUILDER_PATH\n"

#curl  --insecure -XPOST -H "Authorization:token $GHUSERTOKEN" -H "Content-Type:application/zip" --data-binary @${BUILDER_PATH} ${UPLOAD_URL}?name=cslang-builder.zip