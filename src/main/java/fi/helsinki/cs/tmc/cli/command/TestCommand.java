package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
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
        options.addOption("c", false, "colour test");
    }

    @Override
    public void run(CommandLine args, Io io) {
        if (args.hasOption("a")) {
            io.println("Let's run easter egg with -a");
        } else if (args.hasOption("c")) {
            String string = "NORMAL\n"
                    + Color.colorString("BLACK\n", Color.ANSI_BLACK)
                    + Color.colorString("RED\n", Color.ANSI_RED)
                    + Color.colorString("GREEN\n", Color.ANSI_GREEN)
                    + Color.colorString("YELLOW\n", Color.ANSI_YELLOW)
                    + Color.colorString("BLUE\n", Color.ANSI_BLUE)
                    + Color.colorString("PURPLE\n", Color.ANSI_PURPLE)
                    + Color.colorString("CYAN\n", Color.ANSI_CYAN)
                    + Color.colorString("WHITE\n", Color.ANSI_WHITE);
            ExternalsUtil.showStringInPager(string, "colour-test");
        } else {
            io.println("Let's run easter egg.");
        }
    }
}
