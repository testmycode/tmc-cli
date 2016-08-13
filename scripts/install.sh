#!/bin/bash

RCFILE=".bashrc"
RCPATH="$HOME/$RCFILE"

echo "~ Installing TMC-CLI ~"
echo "This install script assumes you are using Bash."
echo "If you are using another shell, please add 'source $HOME/.tmc-autocomplete.sh' in your shell's rc file after TMC-CLI has been installed."
echo ""

if [ ! -f $RCPATH ]; then
	echo -e "$RCPATH not found, creating"
	echo ""
	touch $RCPATH
fi

if [ -f $HOME/.tmc-autocomplete.sh ]; then
# If .tmc-autocomplete.sh is already in user's home dir, don't download it
	if ! ((grep -Fxq "source $HOME/.tmc-autocomplete.sh" $RCPATH) \
	|| (grep -Fxq "source \$HOME/.tmc-autocomplete.sh" $RCPATH) \
	|| (grep -Fxq "source ~/.tmc-autocomplete.sh" $RCPATH)); then
		echo -e ".tmc-autocomplete already exists in $HOME, but is not sourced in $RCFILE"
		echo -e "Adding to $RCPATH"
		echo "" >> $RCPATH
		echo "source $HOME/.tmc-autocomplete.sh" >> $RCPATH
		exit
	else
		echo "TMC-CLI is already installed. Did you forget to source your rc file?"
		echo -e "Try 'source $RCPATH'."
		exit
	fi
fi
echo "Fetching latest release URL"
URL=$(curl -s https://api.github.com/repos/testmycode/tmc-cli/releases/latest | grep '"browser_download_url"' | grep '/tmc"' | head -n 1 | cut -d '"' -f 4)
echo "Downloading TMC-CLI"
curl -LO $URL > ./tmc
if [ ! -f ./tmc ]; then
	echo "Error downloading TMC-CLI"
	exit 1
fi
chmod u+x ./tmc
./tmc > /dev/null
echo "Installation complete"
exit
