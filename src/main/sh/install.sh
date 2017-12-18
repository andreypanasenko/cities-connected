#!/usr/bin/env bash

targetDir="target/classes"
path=`pwd`
rootPackage="org"
echo "Install script run in $path"
if [ -f "Connected.class" ]; then
    rm Connected.class
fi
if [ -d "$rootPackage" ]; then
    rm -rf $rootPackage
fi
if [ -d "$targetDir" ]; then
    cp -a $targetDir/* .
fi
