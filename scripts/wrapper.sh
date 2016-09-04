#!/bin/bash

set -euo pipefail

# find newest jar file
jar_file=$(ls -t target/tmc-cli-*.jar | head -1)

# escape the string argument $1 for using it as substitution string in sed
quotePerlSubst() {
	IFS= read -d '' -r < <(sed -e ':a' -e '$!{N;ba' -e '}' -e 's/[$/\]/\\&/g; s/\n/\\&/g' <<<"$1")
	printf %s "${REPLY%$'\n'}"
}

content=$(cat scripts/stub.sh)

ac_content=$(quotePerlSubst "$(cat scripts/autocompletion.sh)")
content=$(echo "$content" | sed "s/\#EMBED_AUTOCOMPLETE_SH/$ac_content/g")

if [ "${1-}" = "--with-unit-tests" ] ; then
	ac_content=$(quotePerlSubst "$(cat scripts/unit_tests.sh)")
	content=$(echo "$content" | \
		sed "s/\#EMBED_UNIT_TESTS_SH/$ac_content/g")
fi

echo "$content" > tmc

cat "$jar_file" >> tmc && chmod +x tmc

chmod +x tmc
