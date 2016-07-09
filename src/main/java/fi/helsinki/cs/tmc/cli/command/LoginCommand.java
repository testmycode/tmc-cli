package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.backend.TmcUtil;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

@Command(name = "login", desc = "Login to TMC server")
public class LoginCommand extends AbstractCommand {

    private CliContext ctx;
    private Io io;

    private String serverAddress;
    private String username;
    private String password;

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

        if (!ctx.loadBackendWithoutLogin()) {
            return;
        }

        CourseInfo info = ctx.getCourseInfo();
        if (info != null) {
            serverAddress = info.getServerAddress();
            username = info.getUsername();
        }

        serverAddress = getLoginInfo(args, serverAddress, "s", "server address: ");
        username = getLoginInfo(args, username, "u", "username: ");
        password = getLoginInfo(args, null, "p", "password: ");

        Account account = new Account(serverAddress, username, password);
        if (!TmcUtil.tryToLogin(ctx, account)) {
            return;
        }
        if (!SettingsIo.save(account)) {
            io.println("Failed to write the accounts file.");
            return;
        }

        io.println("Login successful.");
    }

    private String getLoginInfo(CommandLine line, String oldValue, String option,
            String prompt) {
        String value = oldValue;

        if (line.hasOption(option)) {
            value = line.getOptionValue(option);
        }

        if (value == null && option.equals("p")) {
            value = io.readPassword(prompt);
        } else if (value == null) {
            value = io.readLine(prompt);
        }
        return value;
    }
}
