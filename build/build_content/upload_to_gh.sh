#!/bin/bash

REPO=cloudslang/cloud-slang
GHUSERTOKEN=$GH_TOKEN
BRANCH=$GIT_BRANCH
BUILD_DIR=$WORKSPACE

CLI_DIR=build/target/cloudslang-cli
CLI_ZIP_PATH=${CLI_DIR}/cslang-cli.zip
CLI_GZIP_PATH=${CLI_DIR}/cslang-cli.tar.gz
BUILDER_PATH=build/target/cslang-builder/cslang-builder.zip

cd ${BUILD_DIR}

echo -e "\nTesting a simple flow"

bash ${CLI_DIR}/cslang/bin/cslang run --f ${CLI_DIR}/cslang/content/io/cloudslang/base/print/print_text.sl --i text=hi

RELEASES_URL="https://api.github.com/repos/${REPO}/releases"

echo -e "\nFetching ${RELEASES_URL}/tags/${BRANCH}\n"

curl https://api.github.com/repos/${REPO}/releases/tags/${BRANCH} > ${BUILD_DIR}/res.json

#Get release id
RELEASEID=$(grep -e id ${BUILD_DIR}/res.json | head -n 1 | awk '{print $2}' | grep -oe '[0-9]*')


echo -e "\n\nUpoloading artifacts using curl\n=================================================================================\n"

UPLOAD_URL="${RELEASES_URL}/$RELEASEID/assets"

FILESIZE=`stat -c '%s' "$CLI_ZIP_PATH"`
curl -s -H "Authorization:token ${GHUSERTOKEN}" -H "Content-Type:application/zip" --data-binary "@${CLI_ZIP_PATH}" "${UPLOAD_URL}?name=cslang-cli-with-content.zip&size=$FILESIZE"

FILESIZE=`stat -c '%s' "$CLI_GZIP_PATH"`
curl -s -H "Authorization:token ${GHUSERTOKEN}" -H "Content-Type:application/gzip" --data-binary "@${CLI_GZIP_PATH}" "${UPLOAD_URL}?name=cslang-cli-with-content.tar.gzip&size=$FILESIZE"

echo -e "\n\nRemoving pre-release from release"
curl -XPATCH -H "Authorization:token ${GHUSERTOKEN}" -d '{"prerelease": false}' 'https://api.github.com/repos/${REPO}/releases/$RELEASEID'