package fi.helsinki.cs.tmc.cli.command.hidden;

import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.core.CommandFactory;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@Command(name = "shell-helper", desc = "Used by autocomplete to extract internal values.")
public class ShellHelperCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {
        options.addOption("c", false, "List of all commands");
        options.addOption("f", false, "Flags of the main program");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        Io io = context.getIo();

        if (args.hasOption("c")) {
            for (Class<Command> commandClass : CommandFactory.getCommands()) {
                Command command = CommandFactory.getCommand(commandClass);
                io.println(command.name());
            }
        } else {
            io.println("This is only for internal usage.");
        }
    }
}
