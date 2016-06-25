package fi.helsinki.cs.tmc.cli.command.core;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.junit.Test;

public class AbstractCommandTest {
    AbstractCommand emptyCommand;
    TestIo io;

    @Command(name = "empty", desc = "Long description")
    private class EmptyCommand extends AbstractCommand {

        @Override
        public void run(CommandLine args, Io io) {
        }

        @Override
        public void getOptions(Options options) {
        }
    }
    
    public AbstractCommandTest() {
        emptyCommand = new EmptyCommand();
        io = new TestIo();
    }

    @Test
    public void helpMessagePrints() {
        String[] args = {"-h"};
        emptyCommand.execute(args, io);
        io.assertContains("tmc empty");
    }

    @Test
    public void emptyCommandHasHelpOption() {
        String[] args = {"-h"};
        emptyCommand.execute(args, io);
        io.assertContains("--help");
    }

    @Test
    public void failWhenInvalidOption() {
        String[] args = {"-a34t3"};
        emptyCommand.execute(args, io);
        io.assertContains("Invalid command");
    }
}