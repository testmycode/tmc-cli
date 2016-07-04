#!/bin/bash

# SCRIPT_PATH will be filled by stub script
alias tmc="$SCRIPT_PATH/tmc"

TMC_COMMANDS=( "\$(tmc shell-helper -c)" )

_tmc_opts()
{
	local cur
	local sub_command
	local main_args

	COMPREPLY=()
	cur="\${COMP_WORDS[COMP_CWORD]}"

	main_args=( "\${COMP_WORDS[@]:0:COMP_CWORD}" )
	for word in "\${main_args[@]}"; do
		if [[ "\$TMC_COMMANDS" =~ "\$word" ]]; then
			sub_command="\$word"
		fi
	done

	case "\$cur" in
	    -*)
		COMPREPLY=( \$( compgen -W "-v -h --version --help" -- "\$cur" ) )
		return 0
		;;
	esac

	if [[ ! -z "\$sub_command" ]]; then
		# use a hack to enable file mode in bash < 4
		# this is from git's autocompletion script
		compopt -o nospace
		compopt -o filenames +o nospace 2>/dev/null ||
		compgen -f /non-existing-dir/ > /dev/null
		return 0
	fi

	COMPREPLY=( \$( compgen -W "\${TMC_COMMANDS}" -- "\$cur" ) )
	return 0
}

complete  -o bashdefault -o default -F _tmc_opts tmc \
        || complete -o default -F _tmc_opts tmc
