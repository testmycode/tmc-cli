package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@Command(name = "logout", desc = "Logout from TMC server")
public class LogoutCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        SettingsIo.delete();
        context.getIo().println("Logged out.");
    }
}
