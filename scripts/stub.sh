#!/bin/bash

set -euo pipefail

## Embeded binary magic

MYSELF=$(which "$0" 2>/dev/null)
[ $? -gt 0 ] && [ -f "$0" ] && MYSELF="./$0"

## Find the java binary and correct version

JAVA_BIN=java
JAVA_HOME=${JAVA_HOME-}
if [ -n "$JAVA_HOME" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
fi

if ! hash "$JAVA_BIN" 2>/dev/null; then
    echo "Java not installed. If you have installed it in another directory then set the \$JAVA_HOME variable."
    exit 1
fi

JAVA_VERSION=$("$JAVA_BIN" -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [ "$JAVA_VERSION" \< "1.7" ]; then
    echo "You must have at least Java 1.7 installed."
    exit 1
fi

## find the place for running the autocomplete/alias file

tmc_detect_profile() {
	local PROFILE_ENV
	local SHELL_ENV
	local HOME_ENV

        PROFILE_ENV=${PROFILE-}
        SHELL_ENV=${SHELL-}
        HOME_ENV=${HOME-}

	if [ -n "$HOME_ENV" ] && [ -f "$PROFILE_ENV" ]; then
		(>&2 echo "Home environment variable is not set")
		return
	fi

	if [ -n "$PROFILE_ENV" ] && [ -f "$PROFILE_ENV" ]; then
		echo "$PROFILE_ENV"
		return
	fi

	local DETECTED_PROFILE
	DETECTED_PROFILE=''
	local SHELLTYPE
	SHELLTYPE="$(basename "/$SHELL_ENV")"

	if [ "$SHELLTYPE" = "bash" ]; then
		if [ -f "$HOME_ENV/.bashrc" ]; then
			DETECTED_PROFILE="$HOME_ENV/.bashrc"
		elif [ -f "$HOME_ENV/.bash_profile" ]; then
			DETECTED_PROFILE="$HOME_ENV/.bash_profile"
		fi
	elif [ "$SHELLTYPE" = "zsh" ]; then
		DETECTED_PROFILE="$HOME_ENV/.zshrc"
	fi

	if [ -z "$DETECTED_PROFILE" ]; then
		if [ -f "$HOME_ENV/.profile" ]; then
			DETECTED_PROFILE="$HOME_ENV/.profile"
		elif [ -f "$HOME_ENV/.bashrc" ]; then
			DETECTED_PROFILE="$HOME_ENV/.bashrc"
		elif [ -f "$HOME_ENV/.bash_profile" ]; then
			DETECTED_PROFILE="$HOME_ENV/.bash_profile"
		elif [ -f "$HOME_ENV/.zshrc" ]; then
			DETECTED_PROFILE="$HOME_ENV/.zshrc"
		fi
	fi

	if [ ! -z "$DETECTED_PROFILE" ]; then
		echo "$DETECTED_PROFILE"
	fi
}

## Bash autocompletion script extraction

# This is used in autocompletion file
SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

tmc_autocomplete_file() {
	echo "${TMC_AUTOCOMPLETE_FILE-$HOME/.tmc-autocomplete.sh}"
}

## Create the alias and autocompletion code if tmc alias not set
tmc_update_autocomplete() {
	local AUTOCOMPLETE_FILE
	local PROFILE_FILE

	AUTOCOMPLETE_FILE="$(tmc_autocomplete_file)"

	cat > "$AUTOCOMPLETE_FILE" <<- EOM
#EMBED_AUTOCOMPLETE_SH
EOM
	chmod +x "$AUTOCOMPLETE_FILE"

	PROFILE_FILE=$(tmc_detect_profile)

	# get the aliases
	set +euo pipefail
	source $PROFILE_FILE
	set -euo pipefail

	if type tmc &> /dev/null; then
		exit
	fi

	if [ -z "$PROFILE_FILE" ]; then
		echo "Profile file not found" >&2
		echo "Put the \"source $AUTOCOMPLETE_FILE\" line in somewhere where" >&2
		echo "it's run at terminal initialization." >&2
	fi
	echo "source $AUTOCOMPLETE_FILE" >> "$PROFILE_FILE"

	echo "To use new autocompletion run the following on command line:" >&2
	echo ". ~/.bashrc" >&2
}

## Auto update code

##### If you MODIFY the install script then do the following:
##### Enable the "THE INSTALL SCRIPT DEBUGGING LINE" at [Tmc]CliUpdater.java
##### (It runs the dev script instead of the latest release script)
##### And use the --force-update flag in application.

tmc_update() {
	tmc_update_autocomplete
}

tmc_install_update() {
	echo "Please report any error messages that may come up below." >&2
	if [ ! -f tmc.new ]; then
		echo "Could not find the updated file." >&2
		exit 127
	fi

	echo "Moving the tmc files..." >&2
	if ! mv tmc tmc.orig; then
		echo "Failed to backup the original tmc binary" >&2
		exit 128
	fi
	if ! mv tmc.new tmc; then
		echo "Failed to replace the original binary with new version" >&2
		echo "You can replace manually the $PWD/tmc with $PWD/tmc.new" >&2
		exit 129
	fi

	rm tmc.orig &> /dev/null
	echo "Running the new tmc update script..." >&2
	echo "" >&2
	tmc_update

	echo ""
	if [ -f tmc ]; then
		echo "Tmc cli installation was successful" >&2
	else
		echo "Tmc cli installation failed." >&2
		exit 127
	fi
	exit
}

if [ "${1-}" == "++internal-update" ]; then
	tmc_install_update
fi

if [ ! -f "$(tmc_autocomplete_file)" ]; then
	tmc_update_autocomplete
	exit
fi

#EMBED_UNIT_TESTS_SH

export COLUMNS=$(tput cols)
exec "$JAVA_BIN" -jar "$MYSELF" "$@"

exit 0 
