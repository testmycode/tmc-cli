#!/bin/bash

# SCRIPT_PATH will be filled by stub script
alias tmc="$SCRIPT_PATH/tmc"

_tmc_opts()
{
  local cur
  # Pointer to current completion word.
  # By convention, it's named "cur" but this isn't strictly necessary.

  COMPREPLY=()
  cur=\${COMP_WORDS[COMP_CWORD]}

  case "\$cur" in
    -*)
      COMPREPLY=( \$( compgen -W "-v -h --version --help" -- \$cur ) )
      return 0
      ;;
#   xx) May add more cases here.
#   yy)
#   zz)
  esac

  COMPREPLY=( \$( compgen -W "help download list-courses easter-egg list-exercises login" -- \$cur ) )
  return 0
}

complete -F _tmc_opts tmc
