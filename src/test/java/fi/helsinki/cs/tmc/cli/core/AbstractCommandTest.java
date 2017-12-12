package fi.helsinki.cs.tmc.cli.core;

import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.cli.io.WorkDir;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.junit.Test;

public class AbstractCommandTest {

    private AbstractCommand emptyCommand;
    private CliContext ctx;
    private TestIo io;

    @Command(name = "empty", desc = "Long description")
    private class EmptyCommand extends AbstractCommand {

        @Override
        public void run(CliContext context, CommandLine args) {}

        @Override
        public void getOptions(Options options) {}

        public void runMethodPrintUsage(CliContext context) {
            printUsage(context);
        }
    }

    @Command(name = "usage-command", desc = "Description")
    private class CommandWithUsage extends AbstractCommand {

        @Override
        public String[] getUsages() {
            return new String[]{"OPTION"};
        }

        @Override
        public void run(CliContext context, CommandLine args) {}

        @Override
        public void getOptions(Options options) {}

        public void runMethodPrintUsage(CliContext context) {
            printUsage(context);
        }
    }

    public AbstractCommandTest() {
        emptyCommand = new EmptyCommand();
        io = new TestIo();
        ctx = new CliContext(io, null, new WorkDir(), new Settings(), null);
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

    @Test
    public void verifyUsageOfEmptyCommand() {
        String[] args = {};
        ((EmptyCommand)emptyCommand).runMethodPrintUsage(ctx);
        io.assertContains("tmc empty");
    }

    @Test
    public void verifyUsageOfCommandWithCustomUsage() {
        String[] args = {};
        CommandWithUsage usageCommand = new CommandWithUsage();
        usageCommand.runMethodPrintUsage(ctx);
        io.assertContains("tmc usage-command OPTION");
    }
}
