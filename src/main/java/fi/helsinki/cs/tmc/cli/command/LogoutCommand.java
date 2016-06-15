package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@Command(name = "logout", desc = "Logout from TMC server")
public class LogoutCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CommandLine args, Io io) {
        SettingsIo.delete();
        io.println("Logged out.");
    }
}
