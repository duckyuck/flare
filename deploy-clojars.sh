#!/bin/sh

for MODULE in flare-core flare-clojure-test flare-midje
do
  cd $MODULE
  lein do clean, test, jar, pom
  scp pom.xml target/flare-*.jar clojars@clojars.org:
  cd ..
done
