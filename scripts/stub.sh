#!/bin/bash
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"

java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi

if ! hash "$java" 2>/dev/null; then
    echo "Java not installed. If you have installed it in another directory then set the \$JAVA_HOME variable."
    exit 1
fi

version=$("$java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [ "$version" \< "1.7" ]; then
    echo "You must have 1.7 installed."
    exit 1
fi

AUTOCOMPLETE="$HOME/.tmc-autocomplete.sh"

# this is used in autocompletion file
SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ ! -f $AUTOCOMPLETE ]; then
	cat > $AUTOCOMPLETE <<- EOM
TMC_AUTOCOMPLETE_SH
EOM

	echo ". $AUTOCOMPLETE" >> ~/.bashrc

	chmod +x $AUTOCOMPLETE
	. $AUTOCOMPLETE
fi

export COLUMNS=$(tput cols)
exec "$java" $java_args -jar $MYSELF "$@"
exit 0 
