#!/bin/bash

BUILD_DIR=$WORKSPACE

REPO=cloudslang/cloud-slang
GHUSERTOKEN=$GH_TOKEN
BRANCH=$GIT_BRANCH

CLI_DIR=${WORKSPACE}/build/target/cli
CLI_ZIP_PATH=${CLI_DIR}/cslang-cli.zip
CLI_GZIP_PATH=${CLI_DIR}/cslang-cli.tar.gz

cd ${BUILD_DIR}

echo -e "\nTesting a simple flow"

bash ${CLI_DIR}/cslang-cli/bin/cslang run --f ${CLI_DIR}/cslang-cli/content/io/cloudslang/base/print/print_text.sl --i text=hi

RELEASES_URL="https://api.github.com/repos/${REPO}/releases"

echo -e "\nFetching ${RELEASES_URL}/tags/${BRANCH}\n"

curl ${RELEASES_URL}/tags/${BRANCH} > ${BUILD_DIR}/res.json

#Get release id
RELEASEID=$(grep -e id ${BUILD_DIR}/res.json | head -n 1 | awk '{print $2}' | grep -oe '[0-9]*')

echo -e "\n\nUploading artifacts using curl\n=================================================================================\n"

UPLOAD_URL="https://uploads.github.com/repos/${REPO}/releases/${RELEASEID}/assets"

FILESIZE=`stat -c '%s' "$CLI_ZIP_PATH"`
curl -X POST -s -H "Authorization:token ${GHUSERTOKEN}" -H "Content-Type:application/zip" --data-binary "@${CLI_ZIP_PATH}" "${UPLOAD_URL}?name=cslang-cli-with-content.zip&size=$FILESIZE"

FILESIZE=`stat -c '%s' "$CLI_GZIP_PATH"`
curl -X POST -s -H "Authorization:token ${GHUSERTOKEN}" -H "Content-Type:application/gzip" --data-binary "@${CLI_GZIP_PATH}" "${UPLOAD_URL}?name=cslang-cli-with-content.tar.gzip&size=$FILESIZE"

# Processing pre-release
PRE_RELEASE_VALUE='false'
PRE_RELEASE_MESSAGE="\n\nMarking as release"
for VALUE in 'true' 'True' 'TRUE' 'yes'
do
  if [ "${PRE_RELEASE}" = "${VALUE}" ]
  then
    PRE_RELEASE_VALUE='true'
    PRE_RELEASE_MESSAGE="\n\nMarking as pre-release"
    continue
  fi
done
echo -e ${PRE_RELEASE_MESSAGE}
REQUEST_BODY='{"prerelease": '${PRE_RELEASE_VALUE}'}'
curl -XPATCH -H "Authorization:token ${GHUSERTOKEN}" -d "${REQUEST_BODY}" "${RELEASES_URL}/${RELEASEID}"