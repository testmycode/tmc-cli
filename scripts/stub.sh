#!/bin/bash

set -euo pipefail

if [[ ${TMC_DEBUG-} == 1 ]]; then
	tmc_debug() {
		(>&2 echo "$*")
	}
else
	tmc_debug() {
		:
	}
fi

tmc_is_native() {
	# get the script directory
	local DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

	TMC_NATIVE_PACKAGE=0

	# check if the tmc binary is in read-only directory
	if [[ ! -w $DIR ]]; then
		TMC_NATIVE_PACKAGE=1
	fi

	if [[ "$TMC_NATIVE_PACKAGE" == "1" ]]; then
		tmc_debug "Tmc is installed through package manager"
	fi

	echo "$TMC_NATIVE_PACKAGE"
}

TMC_NATIVE_PACKAGE=$(tmc_is_native)

tmc_get_binary() {
	tmc_debug "Embeded binary magic"

	local MYSELF=$(which "$0" 2>/dev/null)
	[ $? -gt 0 ] && [ -f "$0" ] && MYSELF="./$0"

	echo "$MYSELF"
}

tmc_find_java_binary() {
	tmc_debug "Find the java binary and correct version"

	local JAVA_BIN=java
	local JAVA_HOME=${JAVA_HOME-}

	if [ -n "$JAVA_HOME" ]; then
		JAVA_BIN="$JAVA_HOME/bin/java"
	fi

	if ! hash "$JAVA_BIN" 2>/dev/null; then
		echo "Java not installed. If you have installed it" >&2
		echo "in another directory then set the \$JAVA_HOME" >&2
		echo "variable." >&2
		exit 1
	fi

	local JAVA_VERSION=$("$JAVA_BIN" -version 2>&1 | awk -F '"' '/version/ {print $2}')

	if [ "$JAVA_VERSION" \< "1.7" ]; then
		echo "You must have at least Java 1.7 installed." >&2
		exit 1
	fi

	echo $JAVA_BIN
}
JAVA_BIN=$(tmc_find_java_binary)

#####
tmc_debug "Find the place for running the autocomplete/alias file"

tmc_detect_profile() {
	local PROFILE_ENV=${PROFILE-}
	local SHELL_ENV=${SHELL-}
	local HOME_ENV=${HOME-}

	if [ -n "$HOME_ENV" ] && [ -f "$PROFILE_ENV" ]; then
		echo "Home environment variable is not set" >&2
		return
	fi

	if [ -n "$PROFILE_ENV" ] && [ -f "$PROFILE_ENV" ]; then
		echo "$PROFILE_ENV"
		return
	fi

	local DETECTED_PROFILE=''
	local SHELLTYPE="$(basename "/$SHELL_ENV")"

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

tmc_binary_file() {
	echo "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/$(basename "${BASH_SOURCE[0]}")"
}

tmc_autocomplete_file() {
	echo "${TMC_AUTOCOMPLETE_FILE-$HOME/.tmc-autocomplete.sh}"
}

## Create the alias and autocompletion code if tmc alias not set
tmc_update_autocomplete() {
	local AUTOCOMPLETE_FILE="$(tmc_autocomplete_file)"
	local INSTALLED=0

	# This variable is defined for the embeded autocompletion file
	SCRIPT_PATH="$(tmc_binary_file)"
	if [[ -e "$AUTOCOMPLETE_FILE" ]]; then
		INSTALLED=1
	fi

	cat > "$AUTOCOMPLETE_FILE" <<- EOM
#EMBED_AUTOCOMPLETE_SH
EOM
	chmod +x "$AUTOCOMPLETE_FILE"

	if [[ $INSTALLED == 0 ]]; then
		tmc_install_hook "$AUTOCOMPLETE_FILE"
	fi
}

tmc_install_hook() {
	local AUTOCOMPLETE_FILE="$1"
	local PROFILE_FILE=$(tmc_detect_profile)

	if [ -z "$PROFILE_FILE" ]; then
		echo "Profile file not found" >&2
		echo "Put the \"source $AUTOCOMPLETE_FILE\" line in" >&2
		echo "your shell's rc file." >&2
	fi
	# The `|| true` structure is used just in case that the rc file
	# has strict mode and user has manually removed tmc script
	echo "source $AUTOCOMPLETE_FILE || true" >> "$PROFILE_FILE"

	echo "To use new autocompletion open a new terminal or run the following command:" >&1
	echo "source $PROFILE_FILE" >&1
}

## Auto update code

##### If you MODIFY the install script then do the following:
##### Enable the "THE INSTALL SCRIPT DEBUGGING LINE" at AutoUpdater.java
##### (It runs your script instead of the script from latest github release)
##### And use the --force-update flag in application.

tmc_update() {
	tmc_update_autocomplete
}

tmc_install_update() {
	if [[ $TMC_NATIVE_PACKAGE == 1 ]]; then
		echo "Tmc should be updated by your package manager" >&2
		exit 126
	fi

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

	if [ -f tmc ]; then
		echo "Tmc cli installation was successful" >&2
	else
		echo "Tmc cli installation failed." >&2
		exit 127
	fi
	exit
}

tmc_uninstall() {
	local AUTOCOMPLETE_FILE="$(tmc_autocomplete_file)"
	local PROFILE_FILE=$(tmc_detect_profile)
	local TMC_FILE="$(tmc_binary_file)"

	#remove the include line from rc file

	grep -v "source $AUTOCOMPLETE_FILE || true" "$PROFILE_FILE" > "${PROFILE_FILE}2"
	if [[ "$?" == 0 ]]; then
		mv "${PROFILE_FILE}2" "$PROFILE_FILE"
	fi

	rm "$AUTOCOMPLETE_FILE"
	rm "$TMC_FILE"
}

tmc_main() {
	if [ "${1-}" == "++internal-update" ]; then
		tmc_install_update
	fi

	if [ "${1-}" == "--uninstall" ]; then
		tmc_uninstall
		exit
	fi

	# check if this is first time running the tmc
	if [ ! -e "$(tmc_autocomplete_file)" ]; then
		tmc_update_autocomplete
		exit
	fi

	local TMC_FLAGS=

	# disable auto updates if tmc is from native package
	if [[ $TMC_NATIVE_PACKAGE == 1 ]]; then
		TMC_FLAGS="-d"
	fi

	#EMBED_UNIT_TESTS_SH

	export COLUMNS=$(tput cols)
	exec "$JAVA_BIN" -jar "$(tmc_get_binary)" $TMC_FLAGS "$@"

	exit 0
}

tmc_main $*
