package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Io;

import fi.helsinki.cs.tmc.cli.tmcstuff.ExternalsUtil;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "paste", desc = "Submit exercise to pastebin")
public class PasteCommand implements CommandInterface {
    private static final Logger logger = LoggerFactory.getLogger(SubmitCommand.class);
    private final Options options;

    private Application app;
    private Io io;

    public PasteCommand(Application app) {
        this.app = app;
        this.options = new Options();
        this.options.addOption("m", "message", true, "Add a message to your paste");
    }

    @Override
    public void run(String[] args, Io io) {
        this.io = io;
        CommandLine line = parseData(args);
        String message = line.getOptionValue("m");
        if (message == null) {
            message = ExternalsUtil.getUserEditedMessage(
                    "\n"
                    + "#Write a message for your paste. "
                    + "Leaving the message empty will abort the paste.\n"
                    + "#Lines beginning with # are comments and will be ignored.\n",
                    "tmc_paste_message.txt",
                    true
            );
        }
        io.print(message);
        if (message == null || message.length() == 0) {
            io.println("Paste message empty, aborting");
            return;
        }
    }

    private CommandLine parseData(String[] args) {
        GnuParser parser = new GnuParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            logger.warn("Unable to parse message.", e);
        }
        return null;
    }


}
