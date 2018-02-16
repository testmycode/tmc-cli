package fi.helsinki.cs.tmc.cli.command;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@Command(name = "logout", desc = "Logout from TMC server")
public class LogoutCommand extends AbstractCommand {

    @Override
    public void getOptions(Options options) {}

    @Override
    public void run(CliContext context, CommandLine args) {
        Io io = context.getIo();
        if (!context.checkIsLoggedIn(false, true)) {
            return;
        }
        context.getAnalyticsFacade().saveAnalytics("logout");
        if (args.getArgs().length > 0) {
            io.errorln("Logout doesn't take any arguments.");
            printUsage(context);
            return;
        }
        SettingsIo.delete();
        context.getSettings().setToken(Optional.absent());
        io.println("Logged out.");
    }
}
