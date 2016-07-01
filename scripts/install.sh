#bin/sh

curl -O https://github.com/tmc-cli/tmc-cli/releases/download/0.6.3/tmc
chmod u+x ./tmc
if ./tmc ;then
	echo Error when installing.
	exit 1
fi


source $HOME/.bashrc

echo Installation complete.
exit 0
