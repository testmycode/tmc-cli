
[![Build Status](https://travis-ci.org/tmc-cli/tmc-cli.svg?branch=master)](https://travis-ci.org/tmc-cli/tmc-cli)
[![GitHub release](https://img.shields.io/badge/release-sprint0-brightgreen.svg?style=flat)](https://github.com/tmc-cli/tmc-cli/releases/latest)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/tmc-cli/tmc-cli/master/LICENSE)
[![Coverage Status](https://coveralls.io/repos/github/tmc-cli/tmc-cli/badge.svg?branch=master)](https://coveralls.io/github/tmc-cli/tmc-cli?branch=master)
[![Build status](https://ci.appveyor.com/api/projects/status/np29sxc72y2f7d57?svg=true)](https://ci.appveyor.com/project/mikkomaa/tmc-cli)
# tmc-cli

TMC-CLI is the command client for Helsinki University's Test My Code -framework. Test My Code is used by various online programming courses for student exercise testing and submission.

##Requirements

* Java Runtime Environment 7
* Bash is required for auto-completion
* Linux, Mac OS X or Microsoft Windows
  * Currently limited support for Windows

##Installation

Download the latest [release](https://github.com/tmc-cli/tmc-cli/releases).
If you use Linux or OS X, choose "tmc". If you use Windows, choose "tmc-cli-<version>.jar".

If you downloaded "tmc", navigate to the download directory on your terminal and make it executable with `chmod u+x ./tmc`.

Launch tmc once with `./tmc`. Running tmc-cli for the first time will add an alias to your .bashrc, enabling you to use tmc-cli by invoking the command "tmc". For the alias to come into effect, execute `. ~/.bashrc` or simply open a new terminal.

To summarise:
```
$ cd Downloads/
$ chmod u+x tmc
$ ./tmc
$ . ~/.bashrc
$ echo "Now you can run tmc anywhere!"
```

If you are using Windows and you downloaded the .jar file, you must use tmc-cli directly with Java like so: `java -jar <path to tmc-cli.jar>`. In the future examples, replace "tmc" with this command. (note: you must have set Java on your system %PATH%. For more information, see [this Java help page](https://www.java.com/en/download/help/path.xml)

##Log in

Once installation is complete, you must login using `tmc login`. This saves your TMC login information to a configuration file in your home directory - you will only have to login once.
```
$ tmc login
username: my-username
password:

```
If your institution uses different server than official Mooc server then you have to give its server address like this:
~~~~
tmc login -s SERVER_ADDRESS
~~~~

Download course
---------------
Get course with following command.
~~~~
tmc download COURSE_NAME
~~~~
If you don't remember the course's official name, then you can get list of courses with command:
~~~~
tmc courses
~~~~

