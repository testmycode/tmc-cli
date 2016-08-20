package fi.helsinki.cs.tmc.cli.command.admin;

import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "admin", desc = "The test command")
public class TestAdminCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(TestAdminCommand.class);
    private Io io;

    @Override
    public void getOptions(Options options) { }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.io = context.getIo();

        io.println("test");
    }
}