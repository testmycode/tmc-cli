package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Class is a test command class.
 */
@Command(name = "easter-egg", desc = "This is an easter egg test command.")
public class TestCommand extends AbstractCommand {
    @Override
    public void getOptions(Options options) {
        options.addOption("a", false, "testikomento");
    }

    @Override
    public void run(CommandLine args, Io io) {
        if (args.hasOption("a")) {
            io.println("Let's run easter egg with -a");
        } else {
            io.println("Let's run easter egg.");
        }
    }
}
