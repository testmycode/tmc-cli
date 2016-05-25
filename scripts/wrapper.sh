#!/bin/sh

# find newest jar file
jar_file=`ls -t target/tmc-cli-*.jar | head -1`

sed '/TMC_AUTOCOMPLETE_SH/ {
	r scripts/autocompletion.sh
d }' scripts/stub.sh > tmc

cat $jar_file >> tmc && chmod +x tmc
