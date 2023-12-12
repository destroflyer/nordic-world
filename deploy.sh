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
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64;mvn clean install

# Deploy (Client)
rm -rf "${CLIENT}"*
mv assets "${CLIENT}"
mv client/target/libs "${CLIENT}"
mv client/target/client-1.0.0.jar "${CLIENT}NordicWorld.jar"
echo "./assets/" > "${CLIENT}assets.ini"
curl -X POST https://destrostudios.com:8080/apps/4/updateFiles

# Deploy (Server)
mv server/target/server-1.0.0-jar-with-dependencies.jar "${SERVER}nordic-world.jar"
mv ecosystem.config.js "${SERVER}"
cd "${SERVER}"
pm2 restart ecosystem.config.js