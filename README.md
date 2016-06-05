
[![Build Status](https://travis-ci.org/tmc-cli/tmc-cli.svg?branch=master)](https://travis-ci.org/tmc-cli/tmc-cli)
[![GitHub release](https://img.shields.io/badge/release-sprint0-brightgreen.svg?style=flat)](https://github.com/tmc-cli/tmc-cli/releases/latest)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/tmc-cli/tmc-cli/master/LICENSE)
[![Coverage Status](https://coveralls.io/repos/github/tmc-cli/tmc-cli/badge.svg?branch=master)](https://coveralls.io/github/tmc-cli/tmc-cli?branch=master)
[![Build status](https://ci.appveyor.com/api/projects/status/np29sxc72y2f7d57?svg=true)](https://ci.appveyor.com/project/mikkomaa/tmc-cli)
# tmc-cli

TMC-CLI is the command client for Helsinki University's Test My Code -framework. Test My Code is used by various online programming courses for exercise testing and submission.

##Requirements

* Java Runtime Environment 7
* Linux, Mac OS X or Microsoft Windows
  * Currently only limited support for Windows
  * Other Unix-like systems may work, but are not tested
* Bash is required for auto-completion - may work on other shells

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
$ echo "Now you can run tmc anywhere."
```

If for some reason the alias was not added to your .bashrc, you can manually add the following line `alias tmc="[PATH_TO_TMC]"`

If you are using Windows and you downloaded the .jar file, you must use tmc-cli directly with Java like so: `java -jar <path to tmc-cli.jar>`. In the future examples, replace "tmc" with this command. (note: you must have set Java on your system %PATH%. For more information, see [this Java help page](https://www.java.com/en/download/help/path.xml)

Now that you've installed tmc-cli, you can view all available commands by running tmc without arguments or with `tmc --help`.

##Logging in

Once installation is complete, you can login using `tmc login`. This saves your TMC login information to a configuration file in ~/.config/tmc-cli/ - you will only have to log in once.
```
$ tmc login
username: my-username
password:
Login successful.
```
By default, tmc-cli connects to Helsinki University MOOC server. To log in to another server, specify the server with the "-s" or "--server" switch:
```
$ tmc login -s [SERVER_ADDRESS]
```

##List courses

Once you have logged in, you can list all the available courses on the server with `tmc list-courses`
```
$ tmc list-courses
java-programming-basics
java-programming-advanced
algorithms-101
c-mooc
javascript-for-lazy-hipsters
```
Note that you can only submit exercises on courses for which you have enrolled.

##Download courses

Navigate to a suitable directory in which you wish to download your courses. Then, run `tmc download [COURSE_NAME]`. This will create a new directory for your course and download all available exercises into it.

```
$ mkdir tmc-courses; cd tmc-courses
~/tmc-courses $ tmc download test-course
Downloading: test-course
[exercise1, exercise2, exercise3, exercise4]
~/tmc-courses $ ls -a
exercise1/ exercise2/ exercise3/ exercise4/ .tmc.json
```
Course-specific information is stored in .tmc.json. Do not manually edit or remove it unless you are completely done with the course - doing so will cause tmc to not function properly.

##Running tests

After you've completed an exercise and wish to run tests on it, navigate to the exercise directory and run `tmc run-tests`. If you are in the course root directory, you can also give the name of the exercise as an argument: `tmc run-tests exercise1`. Running `tmc run-tests` in the course root with not arguments will run tests on all exercises.

```
~/tmc-courses/test-course/exercise1/ $ tmc run-tests
Testing: exercise1
Test results: 1/1 tests passed
All tests passed! Submit to server with 'tmc submit'
```

##Submitting exercises

You have now completed your first exercise! To submit your exercise, run `tmc submit`. The syntax is the same as running tests.

```
~/tmc-courses/test-course/exercise1/ $ tmc submit
Submitting: exercise1
Test results: 1/1 tests passed
All tests passed on server!
Points permanently awarded: [exercise1]
Model solution: https://link.to.model/solution
```

##Disclaimer

This software comes with no warranty. Helsinki University and the tmc-cli developers are not responsible for any damages caused by misuse or misbehaviour of this software.