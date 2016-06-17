#!/bin/bash

set -euo pipefail

MYSELF=$(which "$0" 2>/dev/null)
[ $? -gt 0 ] && [ -f "$0" ] && MYSELF="./$0"

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

AUTOCOMPLETE="$HOME/.tmc-autocomplete.sh"

# this is used in autocompletion file
SCRIPT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

tmc_update_autocomplete() {
	cat > "$AUTOCOMPLETE" <<- EOM
TMC_AUTOCOMPLETE_SH
EOM
	chmod +x "$AUTOCOMPLETE"
}

tmc_update() {
	tmc_update_autocomplete
}

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

if [ ! -f "$AUTOCOMPLETE" ]; then
	tmc_update_autocomplete

	PROFILE_FILE=$(tmc_detect_profile)
	if [ -z "$PROFILE_FILE" ]; then
		echo "Profile file not found"
		echo "Put the \"source $AUTOCOMPLETE\" line in somewhere where"
		echo "it's run at terminal initialization."
	fi
	echo "source $AUTOCOMPLETE" >> "$PROFILE_FILE"
fi

if [ "${1-}" == "++internal-update" ]; then
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

export COLUMNS=$(tput cols)
exec "$JAVA_BIN" -jar "$MYSELF" "$@"

exit 0 
