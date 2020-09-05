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
ABC1="${CLIENT}*"
ABC2="$CLIENT*"
echo "$ABC1"
echo "$ABC2"
echo "------"
rm -rf "$ABC1"
echo "------"
rm -rf "$ABC2"
echo "------"
mv assets "${CLIENT}"
mv client/target/libs "${CLIENT}"
mv client/target/client-1.0.0.jar "${CLIENT}NordicWorld.jar"
echo "./assets/" > "${CLIENT}assets.ini"
curl https://destrostudios.com:8080/apps/4/updateFiles