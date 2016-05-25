#!/bin/bash

_tmc_opts()
{
  local cur
  # Pointer to current completion word.
  # By convention, it's named "cur" but this isn't strictly necessary.

  COMPREPLY=()   # Array variable storing the possible completions.
  cur=${COMP_WORDS[COMP_CWORD]}

  case "$cur" in
    -*)
      COMPREPLY=( $( compgen -W "-v -h --version --help" -- $cur ) )
      return 0
      ;;
#   Generate the completion matches and load them into $COMPREPLY array.
#   xx) May add more cases here.
#   yy)
#   zz)
  esac

  COMPREPLY=( $( compgen -W "help download list-courses easter-egg list-exercises login" -- $cur ) )
  return 0
}

pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

alias tmc="$SCRIPTPATH/../tmc"
complete -F _tmc_opts tmc
