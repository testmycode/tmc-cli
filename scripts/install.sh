#!/bin/bash

set -euo pipefail

echo "~ Installing TMC-CLI ~"
echo "(If your shell is not bash, you may have to do the installation manually.)"
echo ""

echo "Fetching latest release URL"
if ! PAGE=$(curl -s https://api.github.com/repos/testmycode/tmc-cli/releases/latest); then
	echo "Failed to fetch latest release from github api." >&2
	exit
fi
URL=$(echo "$PAGE" | grep '"browser_download_url"' | grep '/tmc"' | head -n 1 | cut -d '"' -f 4)

echo "Downloading TMC-CLI from following address"
echo "$URL"
echo

curl -LO "$URL" > ./tmc || true

if [ ! -f ./tmc ]; then
	echo "Error downloading TMC-CLI"
	exit 1
fi

if [ -f "$HOME/.tmc-autocomplete.sh" ]; then
	rm "$HOME/.tmc-autocomplete.sh" || true
fi

chmod u+x ./tmc
./tmc

echo "source $HOME/.tmc-autocomplete.sh || true" >> "$HOME/.bashrc"

echo "Installation complete. Please restart the terminal."
