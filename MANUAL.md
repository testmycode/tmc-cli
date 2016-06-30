TMC-CLI 1 2016-06-30 "Helsinki Univ. Dep. of CS" "TMC-CLI Manual"
=====================================================================

NAME
----

tmc - Command-line client for TestMyCode

SYNOPSIS
--------

`tmc` [`-h|--help`] [`-v|--version`]
      <`COMMAND`> [`-h|--help`]
      <`COMMAND`> [`options`] [*path*] ...

DESCRIPTION
-----------

`tmc` is an exercise testing and submission utility for students participating
on online courses utilising the free and open TestMyCode-framework. TMC-CLI is
a command-line interface for TMC-CORE, the TestMyCode backend. TMC-CLI's primary
features include downloading exercises, running tests, viewing progress and
submitting completed exercises.

OPTIONS
-------

`-v` `--version`
  Display the version of this build

`-h` `--help`
  Display a concise help message. If issued with a command, display a help
  message for that command.

COMMANDS:
---------



LOGIN
-----

  `tmc` `login` [`-s` *server address*] [`-u` *username*] [`-p` *password*]
  
  Login to TMC server. If credentials are not given as options, the user will
  be asked to input any missing credentials.
  
  `-s` `--server`
  Specify which server to connect to.
    
  `-u` `--user`
  Specify username.
    
  `-p` `--password`
  Specify password.
 
COURSES
-------

  `tmc` `courses`
  
  List all available courses on the server.
  
DOWNLOAD
--------
  
  `tmc` `download` *course*
  
  Download a course from the server.
  
  `-a` `--all`
  Download all exercises, including completed ones.
  
EXERCISES
---------

  `tmc` `exercises` [`-n`] [`-i`]
  
  List the status of all of the course's exercises. Exercises are grouped by
  their deadlines. On Unix, the list is displayed on a pager.
  
  `-n` `--no-pager`
  Print the list directly to the terminal.
  
  `-i` `--internet`
  Fetch exercises from the server, as opposed to reading from the local cache.

UPDATE
------

  `tmc` `update`
  
  Update the course cache and download newly available exercises.
  
TEST
----

  `tmc` `test` [`-a`] [`-d`] [*path*] ...
  
  Run tests for the specified exercise. If no *path* is given, tests will be
  run in the current working directory. Several exercises can be tested at once.
  If the current work directory is the course root directory or the course root
  directory was given as a *path*, then all exercises will be tested.
  
  `-a` `--all`
  Display all test results, instead of only the failed tests.
  
  `-d` `--details`
  Display more detailed error messages.

SUBMIT
------

  `tmc` `submit` [`-a`] [`-d`] [`-c`] [*path*] ...

  Submit exercises to the server. If no *path* is given, the exercise in the
  current working directory will be submitted. Several exercises can be submitted
  at once. If the current work directory is the course root directory or the
  course root directory was given as a *path*, then all exercises will be submitted.
  
  `-a` `--all`
  Display all test results, instead of only the failed tests.
  
  `-d` `--details`
  Display more detailed error messages.
  
  `-c` `--completed`
  Submit all exercises in the current course which have passed local tests.
  
INFO
----

  `tmc` `info` [`-a`] [`-i`] [*course or exercise*]
  
  Display the current status of a course or an exercise. If used for a course,
  shows the total amount of available, completed and locked exercises. If used
  for an exercise, shows the exercise's status and deadline.
  
  `-a` `--all`
  Displays all information for given course and exercises.

  `-i` `--internet`
  Fetches information from the server, as opposed to reading from local cache.
  
FILES
-----

*~/.config/tmc-cli/properties.json*
  User configuration file. Use `tmc prop *KEY* *VALUE* ...` to edit properties.

*~/.config/tmc-cli/accounts.json*
  User login credentials. Use `tmc logout` to safely delete.
  
*~/.config/tmc-cli/logs/tmc-cli.log*
  Debug logging.
  
If `$XDG_CONFIG_HOME` is set, *~/.config/* is replaced with its value.

On Windows, *~/.config/* is replaced with the value of `%APPDATA%`, usually
*C:\\Users\\Username\\AppData\\Roaming\\.*

ENVIRONMENT
-----------

`EDITOR`
  Text editor for editing messages for pastebin and feedback. If unset, defaults
  to `nano` on Unix and `notepad` on Windows.

`PAGER`
  Pager for displaying text files. If unset, defaults to `less -R` on Unix.
  This functionality is broken on Windows, but defaults to `more`.

BUGS
----

Most likely. Please submit bug reports and other issues to [the tmc-cli issue tracker]
(https://github.com/tmc-cli/tmc-cli/issues).

AUTHORS
-------

    Johannes L. [jclc](https://github.com/jclc)
    Matti L. [matike](https://github.com/matike)
    Mikko M. [mikkomaa](https://github.com/mikkomaa)
    Aleksi S. [salmela](https://github.com/salmela)
    Juha V. [juvester](https://github.com/juvester)
