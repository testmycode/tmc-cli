# tmc-cli
[![Build Status](https://travis-ci.org/tmc-cli/tmc-cli.svg?branch=master)](https://travis-ci.org/tmc-cli/tmc-cli)
[![GitHub release](https://img.shields.io/badge/release-sprint0-brightgreen.svg?style=flat)](https://github.com/tmc-cli/tmc-cli/releases/latest)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/tmc-cli/tmc-cli/master/LICENSE)
[![Coverage Status](https://coveralls.io/repos/github/tmc-cli/tmc-cli/badge.svg?branch=master)](https://coveralls.io/github/tmc-cli/tmc-cli?branch=master)
[![Build status](https://ci.appveyor.com/api/projects/status/np29sxc72y2f7d57?svg=true)](https://ci.appveyor.com/project/mikkomaa/tmc-cli)

This is our super awesome command line client for tmc!


Usage
=====

Installing
----------
Run these commands on command line to make the client easier to use.
~~~~
./tmc
. ~/.bashrc
~~~~

Log in
------
After these login with your tmc account with command.
~~~~
tmc login
~~~~
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

