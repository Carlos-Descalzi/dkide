#!/bin/bash

if [ "$IDEPATH" == "" ];then
    echo "usage: IDEPATH=[ide location] $0"
    exit 1
fi

./gradlew buildPlugin

pwd=$PWD
unzip -o build/distributions/dkide-1.0-SNAPSHOT.zip -d $IDEPATH/plugins
rm -rf dkide.zip
pushd `dirname $IDEPATH`
zip -r -9 $pwd/build/distributions/dkide.zip `basename $IDEPATH`
tar cvzf $pwd/build/distributions/dkide.tar `basename $IDEPATH`
popd
pushd build/distributions
rm -rf dkide.tar.bz2
bzip2 dkide.tar
