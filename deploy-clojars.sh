#!/bin/sh
lein do clean, test, jar, pom
scp pom.xml target/flare-*.jar clojars@clojars.org:
