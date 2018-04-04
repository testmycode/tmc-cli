
[![Build Status](https://travis-ci.org/testmycode/tmc-cli.svg?branch=master)](https://travis-ci.org/testmycode/tmc-cli)
[![GitHub release](https://img.shields.io/badge/release-latest-brightgreen.svg?style=flat)](https://github.com/testmycode/tmc-cli/releases/latest)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/testmycode/tmc-cli/master/LICENSE)
[![Coverage Status](https://coveralls.io/repos/github/testmycode/tmc-cli/badge.svg?branch=master)](https://coveralls.io/github/testmycode/tmc-cli?branch=master)
[![Build status](https://ci.appveyor.com/api/projects/status/js9jr4ef4o7dnama?svg=true)](https://ci.appveyor.com/project/jamox/tmc-cli-08d2u)

![tmc-cli logo](https://rawgit.com/testmycode/tmc-cli/master/docs/logo_plain.svg)

# tmc-cli

TMC-CLI is the command-line client for University of Helsinki's TestMyCode -framework. TestMyCode is used by various online programming courses for exercise testing and submission.

![Demonstration](docs/demo.gif)

## Requirements

* Java Runtime Environment 8
* Linux, Mac OS X or Microsoft Windows
  * Currently only limited support for Windows
  * Other Unix-like systems may work, but are not tested
* If you want autocompletion, Bash or a Bash-compatible shell is required

## Installation

### Using the install script (Linux/OS X only)

`cd` into the directory where you wish to install TMC-CLI. Then, copy and paste the following command in your terminal:

```
curl -0 https://raw.githubusercontent.com/testmycode/tmc-cli/master/scripts/install.sh | bash
```

TMC-CLI should now be installed. You can try running it with `tmc` in a new terminal.

### Manual installation

Download the latest [release](https://github.com/testmycode/tmc-cli/releases/latest). If you use Linux or OS X, choose "tmc". If you use Windows, choose "tmc-cli-[VERSION].jar".

If you downloaded "tmc", navigate to the download directory on your terminal and make it executable with `chmod u+x ./tmc`.

Launch tmc once with `./tmc`. Running tmc-cli for the first time will add an alias to your .bashrc, enabling you to use tmc-cli by invoking the command `tmc`. For the alias to come into effect, execute `. ~/.bashrc` or simply open a new terminal.

To summarise:

```
~ $ chmod u+x tmc
~ $ ./tmc
~ $ . ~/.bashrc
~ $ echo "Now you can run tmc anywhere."
```

If for some reason nothing was added to your .bashrc or your shell of choice is not Bash, you can manually add the following line `source $HOME/.tmc-autocomplete.sh` (or `alias tmc="[PATH_TO_TMC]"` for tmc with no autocompletion) to your .bashrc / other shell rc file.

If you are using Windows and you downloaded the .jar file, you must use tmc-cli directly with Java like so: `java -jar [path_to_tmc-cli.jar]`. In the following examples, replace `tmc` with this command. (note: you must have set Java on your system `%PATH%`. For more information, see [this Java help page](https://www.java.com/en/download/help/path.xml).)

Tip: On Windows, use `doskey tmc="java -jar [path_to_tmc-cli.jar] $*"` in cmd.exe or `doskey /exename=powershell.exe tmc="java -jar [path_to_tmc-cli.jar] $*"` in PowerShell to create a convenient alias.

Now that you've installed tmc-cli, you can view all available commands by running tmc without arguments or with `tmc --help`. You can also view all available options for commands by running them with the `--help` switch, for example `tmc courses --help`.

## Uninstallation

Delete tmc from the directory where you downloaded it, .tmc-autocomplete.sh from your home directory and remove 'source $HOME/.tmc-autocomplete' from your shell rc file.

## Manual

The Unix man page for tmc-cli is located in docs/tmc.1 in this repository. To view it, open it with `man -l tmc.1`.

The man page is generated from docs/MANUAL.md using [md2man](https://github.com/sunaku/md2man).

For system administrators/packagers: To make the man page available for all users, move it to /usr/share/man/man1 - then it can be displayed with `man tmc`.

You can also open the manual in your shell remotely with `curl -0s https://raw.githubusercontent.com/testmycode/tmc-cli/master/docs/tmc.1 | man -l -`. This will show the manual for the latest version. You may want to bind it to an alias for ease of use.

## Logging in

Once installation is complete, you can log in using `tmc login`. This saves your TMC login information to a configuration file in ~/.config/tmc-cli/ (or %APPDATA% on Windows) - you will only have to log in once.

```
~ $ tmc login
server address:
username:
password:
Choose organization by writing its slug:
Do you want to send crash reports for client development? (Y/n)
Do you want to send analytics data for research? (Y/n)
```

You can change your organization with the command `organization`. Use the option `-o` and give the slug of an organization as an argument. Otherwise all available organizations will be listed.
You can inspect and change your settings such as sending data with the command `config`. Use the option `-l` to see your current settings and give `[KEY]=[NEW VALUE]` as arguments to change them.

## Listing courses

Once you have logged in, you can list all the available courses on the server with `tmc courses`.
```
~ $ tmc courses
java-programming-basics
java-programming-advanced
algorithms-101
c-mooc
javascript-for-lazy-hipsters
```

Note that you can only exercises on courses for which you have enrolled.

## Downloading courses

Navigate to a suitable directory in which you wish to download your course(s). Then, run `tmc download [COURSE_NAME]`. This will create a new directory for your course and download all available exercises into it. By default, only exercises that you have not fully completed are downloaded - download all exercises with `-a`.

```
~ $ mkdir tmc-courses; cd tmc-courses
~/tmc-courses $ tmc download test-course
Downloading: test-course
[exercise1, exercise2, exercise3, exercise4]
~/tmc-courses $ cd test-course
~/tmc-courses/test-course $ ls -pa
exercise1/  exercise2/  exercise3/  exercise4/  .tmc.json
```

Course-specific information is stored in .tmc.json. Do not manually edit or remove it unless you are completely done with the course - doing so will cause tmc to not function properly.

## Running tests

After you've completed an exercise and wish to run tests on it, navigate to the exercise directory and run `tmc test`. If you are in the course root directory, you can also give the names of the exercises as arguments: `tmc test exercise1 exercise2`. Running `tmc test` in the course root with no arguments will run tests on all exercises.

```
~/tmc-courses/test-course/exercise1 $ tmc test
Testing: exercise1
Test results: 1/1 tests passed
All tests passed! Submit to server with 'tmc submit'
```

## Submitting exercises

You have now completed your first exercise! To submit your exercise, run `tmc submit [exercise-name]` or run `tmc submit` in an exercise directory. The syntax is the same as for running tests.

```
~/tmc-courses/test-course/exercise1 $ tmc submit
Submitting: exercise1
Test results: 1/1 tests passed
All tests passed on server!
Points permanently awarded: [exercise1]
Model solution: https://link.to.model/solution
```

## Updating the course

As you complete exercises, more exercises may become available. To update the course and download new exercises, run `tmc update`.

```
~/tmc-courses/test-course $ tmc update
New exercises downloaded: [exercise5, exercise6]
```

## Listing exercises

If you want to see your current progress, you can view the status of all course exercises with `tmc exercises [course]`. By default, the exercise list is viewed in your system's pager, but you can print them directly to your terminal with the `-n` or `--no-pager` switch.

```
~/tmc-courses/test-course $ tmc exercises test-course -n
Course name: test-course
Deadline: 2038-20-01 at 18:00:00
  Completed: exercise1
  Completed: exercise2
  Attempted: exercise3
  Not completed: exercise4
```

## Sending exercises to pastebin

If you're having trouble with an exercise or just want to have your code peer-reviewed, you can use `tmc paste` to send an exercise to the TMC server's pastebin. You'll be prompted to add a message, but it is optional.

```
~/tmc-courses/test-course/exercise1 $ tmc paste
Zipping project
Submitting project
Paste sent for exercise exercise1
https://link.to.paste/bin
```

You cannot submit exercises that you have completed.

# Contribute

1. [Fork](https://help.github.com/articles/fork-a-repo/) this repository to your own GitHub account and then [clone](https://help.github.com/articles/cloning-a-repository/) it to your local device
2. Install the dependencies: `mvn dependency:go-offline`
3. Build the code : `mvn package`
4. Run the tests : `mvn test`
5. Run the checkstyle : `mvn checkstyle:check`

## Crafting a release
Releases are automatically built in Travis and pushed to GitHub releases. Once ready to release, update version in pom.xml and gat the commit accordingly. Once tests have passed a new release is uploaded to travis. Note: only travis runs with tag set are used for releases.

## Disclaimer

This software is licensed under [the MIT license](https://raw.githubusercontent.com/testmycode/tmc-cli/instructions/LICENSE).

This software comes with no warranty. University of Helsinki and the tmc-cli developers are not responsible for any damages caused by misuse or misbehaviour of this software.
