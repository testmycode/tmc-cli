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

function tmc_update_autocomplete {
	cat > $AUTOCOMPLETE <<- EOM
TMC_AUTOCOMPLETE_SH
EOM
	chmod +x $AUTOCOMPLETE
}

function tmc_update {
	tmc_update_autocomplete
}

if [ ! -f $AUTOCOMPLETE ]; then
	tmc_update_autocomplete

	echo ". $AUTOCOMPLETE" >> ~/.bashrc
	. $AUTOCOMPLETE
fi

if [ "$1" == "!internal-update" ]; then
	if [ ! -f tmc.new ]; then
		echo "Could not find the updated file."
		exit 127
	fi

	echo "Moving the tmc files..."
	mv tmc tmc.orig
	mv tmc.new tmc
	rm tmc.orig
	echo "Running the new tmc update script..."
	tmc_update

	echo ""
	if [ -f tmc ]; then
		echo "Tmc cli installation was successful"
		#echo ""
		#echo "To use new autocompletion run the following on command line:"
		#echo ". ~/.bashrc"
	else
		echo "Tmc cli installation failed."
		exit 127
	fi
	exit
fi

exec "$java" $java_args -jar $MYSELF "$@"

exit 0 
