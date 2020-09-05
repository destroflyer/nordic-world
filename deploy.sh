#!/bin/bash

VERSION=$1
SERVER=$2
CLIENT=$3

# Checkout
git clone https://github.com/destroflyer/nordic-world.git
if [ -n "$VERSION" ]; then
  git checkout "$VERSION"
fi

# Build
mvn clean install

# Deploy (Server)
# mv server/target/server-1.0.0.jar "${SERVER}nordic-world.jar"
# sh "${SERVER}control.sh" restart

# Deploy (Client)
echo "${CLIENT}*"
rm -vrf "${CLIENT}*"
echo "------"
mv assets "${CLIENT}"
mv client/target/libs "${CLIENT}"
mv client/target/client-1.0.0.jar "${CLIENT}NordicWorld.jar"
echo "./assets/" > "${CLIENT}assets.ini"
curl https://destrostudios.com:8080/apps/4/updateFiles