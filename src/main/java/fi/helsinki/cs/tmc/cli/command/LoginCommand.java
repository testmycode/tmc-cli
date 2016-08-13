package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
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
    public void run(CliContext context, CommandLine args) {
        this.ctx = context;
        this.io = ctx.getIo();

        if (!ctx.loadBackendWithoutLogin()) {
            return;
        }

        if (!TmcUtil.hasConnection(ctx)) {
            io.println("You don't have internet connection currently.");
            io.println("Check the tmc-cli logs to get exact problem.");
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

        AccountList list = SettingsIo.loadAccountList();
        list.addAccount(account);
        if (!SettingsIo.saveAccountList(list)) {
            io.println("Failed to write the accounts file.");
            return;
        }

        io.println("Login successful.");
    }

    private String getLoginInfo(CommandLine line, String oldValue, String option, String prompt) {
        String value = oldValue;
        boolean isPassword = option.equals("p");

        if (line.hasOption(option)) {
            value = line.getOptionValue(option);
        }

        if (value != null && !isPassword) {
            io.println(prompt + value);
        }

        if (value == null && isPassword) {
            value = io.readPassword(prompt);
        } else if (value == null) {
            value = io.readLine(prompt);
        }
        return value;
    }
}
