package fi.helsinki.cs.tmc.cli.core;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.junit.Test;

public class AbstractCommandTest {

    AbstractCommand emptyCommand;
    CliContext ctx;
    TestIo io;

    @Command(name = "empty", desc = "Long description")
    private class EmptyCommand extends AbstractCommand {

        @Override
        public void run(CliContext context, CommandLine args) {
        }

        @Override
        public void getOptions(Options options) {
        }
    }

    public AbstractCommandTest() {
        emptyCommand = new EmptyCommand();
        io = new TestIo();
        ctx = new CliContext(io);
    }

    @Test
    public void helpMessagePrints() {
        String[] args = {"-h"};
        emptyCommand.execute(ctx, args);
        io.assertContains("tmc empty");
    }

    @Test
    public void emptyCommandHasHelpOption() {
        String[] args = {"-h"};
        emptyCommand.execute(ctx, args);
        io.assertContains("--help");
    }

    @Test
    public void failWhenInvalidOption() {
        String[] args = {"-a34t3"};
        emptyCommand.execute(ctx, args);
        io.assertContains("Invalid command");
    }
}