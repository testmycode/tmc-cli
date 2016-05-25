#!/bin/sh

# find newest jar file
jar_file=`ls -t target/tmc-cli-*.jar | head -1`

cat scripts/stub.sh $jar_file > tmc && chmod +x tmc
