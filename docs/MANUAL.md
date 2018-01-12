TMC-CLI 1 2016-06-30 "TestMyCode" "TMC-CLI Manual"
==================================================

NAME
----

tmc - Command-line client for TestMyCode

SYNOPSIS
--------

`tmc` [`-h|--help`] [`-v|--version`]
      <`COMMAND`> [`-h|--help`]
      <`COMMAND`> [`arguments`] ...

DESCRIPTION
-----------

`tmc` is an exercise testing and submission utility for students participating
on online courses utilising the free and open TestMyCode-framework. TMC-CLI is
a command-line interface for TMC-CORE, the TestMyCode client backend. TMC-CLI's
primary features include downloading exercises, running tests, viewing progress
and submitting completed exercises.

All functionality is divided between different commands. To use `tmc` for any
meaningful task, you must issue a command after `tmc`. Commands each have their own
options. For more information on commands and their options, see the `COMMAND`
sections below.

OPTIONS
-------

`-v` `--version`
  Display the version of this build.

`-h` `--help`
  Display a concise help message. Shows all commonly used commands. If issued
  with a command, display a help message for that command.

---
COMMANDS
---

---

COMMAND: LOGIN
-----

`tmc` `login` [`-u` *username*] [`-p` *password*]

Login to TMC server. If credentials are not given as options, the user will
be asked to input any missing credentials. You will have to be logged in
in order to use certain commands.

`-u` `--user`
  Specify username.

`-p` `--password`
  Specify password.

COMMAND: LOGOUT
---------------

`tmc` `logout`

Delete login credentials from configurations.

COMMAND: COURSES
----------------

`tmc` `courses`

List all available courses on the server.

COMMAND: DOWNLOAD
-----------------

`tmc` `download` [`-a`] *course*

Download a course from the server.

`-a` `--all`
  Download all exercises, including completed ones.

COMMAND: EXERCISES
------------------

`tmc` `exercises` [`-n`] [`-i`]

List the status of all of the course's exercises. Exercises are grouped by
their deadlines. On Unix, the list is displayed on a pager.

`-n` `--no-pager`
  Print the list directly to the terminal.

`-i` `--internet`
  Fetch exercises' statuses from the server, as opposed to reading from the
  local cache.

COMMAND: UPDATE
---------------

`tmc` `update`

Update the course cache and download newly available exercises.

COMMAND: TEST
-------------

`tmc` `test` [`-a`] [`-d`] [*path*] ...

Run tests for the specified exercise. If no *path* is given, tests will be
run in the current working directory. Several exercises can be tested at once.
If the current work directory is the course root directory or the course root
directory was given as a *path*, then all exercises will be tested.

`-a` `--all`
  Display all test results, instead of only the failed tests.

`-d` `--details`
  Display more detailed error messages.

COMMAND: SUBMIT
---------------

`tmc` `submit` [`-a`] [`-d`] [`-c`] [*path*] ...

Submit exercises to the server. If no *path* is given, the exercise in the
current working directory will be submitted. The submittable exercise(s) must be given as an argument.

For every successful submission, you'll be prompted to send feedback for the
exercise if the course has enabled feedback questions. Sending feedback is
always optional.

`-a` `--all`
  Display all test results, instead of only the failed tests.

`-d` `--details`
  Display more detailed error messages.

`-c` `--completed`
  Submit all exercises in the current course which have passed local tests.

COMMAND: INFO
-------------

`tmc` `info` [`-a`] [`-i`] [*course or exercise*]

Display the current status of a course or an exercise. If used for a course,
shows the total amount of available, completed and locked exercises. If used
for an exercise, shows the exercise's status and deadline.

`-a` `--all`
  Displays all information for given course and exercises.

`-i` `--internet`
  Fetches information from the server, as opposed to reading from local cache.

COMMAND: PASTE
--------------

`tmc` `paste` [`-o`] [`-n`] [`-m` *message*] [*exercise*]

Submit an exercise to the tmc-pastebin. You can attach a message to your paste.
Once submission is successful, a shareable link will be printed.

`-o` `--open`
  Open the link to the paste in the default internet browser after submission.

`-n` `--no-message`
  Do not send a message alongside the paste.

`-m` `--message`
  Give the message as an argument instead of opening a text editor.

COMMAND: CONFIG
-------------

`tmc` `config` `[-q]`[*KEY=VALUE*] ...
      `config` `-d` `[-q]` [*KEY*] ...
      `config` `-l`
      `config` `-g` [*KEY*] or `-g`=[*KEY*]

Set or unset TMC-CLI properties. Only accepts certain keys. If not invoked with the option `quiet` will ask to confirm changes.

`-d` `--delete`
  Unset given properties.

`-q` `--quiet`
  Do not ask for confirmations or print out set values.

`-l` `--list`
  List all current settings.

`-g` `--get=KEY` or `--get KEY`
  Get the value of a specific key.

List of configurable settings:

* *update-date*
  Scheduled time for the next version check. Stored in properties.
* *testresults-left* *testresults-right* *progressbar-left* *progressbar-right*
  Change progress bar colours. Recognised values: black, red, green, yellow,
  blue, purple, cyan, white, none. Stored in properties.
* *send-diagnostics*
  Allow sending crash reports and analytics for client development. Stored with account.
* *send-analytics*
  Allow sending analytics data of commands run. Stored in account.
* *server-address*
  Address to fetch courses from and submit to. Defaults to `https://tmc.mooc.fi`. Stored with account.

COMMAND: ORGANIZATION
---------------

`tmc` `organization` [`-o`][*slug*] ...

Change organization, which determines the downloadable courses. If no slug is given, or the slug is invalid, all available organizations are listed, and the user will be prompted to choose one.

`-o` `--organization`
Change organization to slug given as an argument without the listing of all organizations.

---

FILES
-----

`[course directory]/.tmc.json`
  Course configuration and cache file. Saves the status of the username, server
  address and course's exercises. Manually editing this file may have adverse
  effects.

`~/.config/tmc-cli/properties.json`
  User configuration file. Use `tmc prop` to edit properties.

`~/.config/tmc-cli/accounts.json`
  User login credentials. Use `tmc logout` to safely delete.

`~/.config/tmc-cli/logs/tmc-cli.log`
  Debug logging.

For more on config locations, see `ENVIRONMENT` -> `XDG_CONFIG_HOME` and `APPDATA`.

ENVIRONMENT
-----------

`EDITOR`
  Text editor for editing messages for pastebin and feedback. If unset, defaults
  to `nano` on Unix and `notepad` on Windows.

`PAGER`
  Pager for displaying text files. If unset, defaults to `less -R` on Unix.
  This functionality is broken on Windows, but defaults to `more`.

`XDG_CONFIG_HOME`
  If set on Unix, *~/.config/* in config file paths is replaced with its value.

`APPDATA`
  On Windows, *~/.config/* is replaced with the value of `%APPDATA%`, usually
  *C:\\Users\\Username\\AppData\\Roaming\\.* If `%APPDATA%` is unset, user's home
  directory will be used instead.

BUGS
----

Most likely. Please submit bug reports, spelling and grammar corrections and
other issues to [the tmc-cli issue tracker](https://github.com/tmc-cli/tmc-cli/issues).

AUTHORS
-------

    Johannes L. [jclc](https://github.com/jclc)
    Matti L. [matike](https://github.com/matike)
    Mikko M. [mikkomaa](https://github.com/mikkomaa)
    Aleksi S. [salmela](https://github.com/salmela)
    Juha V. [juvester](https://github.com/juvester)
