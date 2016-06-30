package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@Command(name = "login", desc = "Login to TMC server")
public class LoginCommand extends AbstractCommand {

    private CliContext ctx;
    private Io io;

    @Override
    public void getOptions(Options options) {
        options.addOption("u", "user", true, "TMC username");
        options.addOption("p", "password", true, "Password for the user");
        options.addOption("s", "server", true, "Address for TMC server");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.ctx = getContext();
        this.io = io;
        String serverAddress = getLoginInfo(args, "s", "server address: ");
        String username = getLoginInfo(args, "u", "username: ");
        String password = getLoginInfo(args, "p", "password: ");

        if (!ctx.loadBackend(false)) {
            return;
        }

        //TODO don't create new settings object.  
        Settings settings = new Settings(serverAddress, username, password);
        if (TmcUtil.tryToLogin(ctx, settings) && saveLoginSettings(settings)) {
            io.println("Login successful.");
        }
    }

    private String getLoginInfo(CommandLine line, String option, String prompt) {
        String info = line.getOptionValue(option);
        if (info == null && option.equals("p")) {
            info = io.readPassword(prompt);
        } else if (info == null) {
            info = io.readLine(prompt);
        }
        return info;
    }

    private boolean saveLoginSettings(Settings settings) {
        if (SettingsIo.save(settings)) {
            return true;
        } else {
            io.println("Login failed.");
            return false;
        }
    }
}
