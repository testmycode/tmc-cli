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

import fi.helsinki.cs.tmc.core.domain.Organization;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.common.base.Optional;

import java.util.concurrent.Callable;

@Command(name = "login", desc = "Login to TMC server")
public class LoginCommand extends AbstractCommand {

    private CliContext ctx;
    private Io io;

    private String username;
    private String password;

    @Override
    public String[] getUsages() {
        return new String[] {"[-u=USERNAME] [-p=PASSWORD] [-s=SERVER_ADDRESS]"};
    }

    @Override
    public void getOptions(Options options) {
        options.addOption("u", "user", true, "TMC username");
        options.addOption("p", "password", true, "Password for the user");
        options.addOption("o", "organization", true, "TMC organization");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.ctx = context;
        this.io = ctx.getIo();

        if (args.getArgs().length > 0) {
            io.errorln("Login doesn't take any arguments.");
            printUsage(context);
            return;
        }

        this.ctx.loadUserInformation();

        this.ctx.getAnalyticsFacade().saveAnalytics("login");

        if (!TmcUtil.hasConnection(ctx)) {
            io.errorln("You don't have internet connection currently.");
            io.errorln("Check the tmc-cli logs if you disagree.");
            return;
        }

        CourseInfo info = ctx.getCourseInfo();
        if (info != null) {
            username = info.getUsername();
        }

        username = getLoginInfo(args, username, "u", "username: ");
        password = getLoginInfo(args, null, "p", "password: ");

        OrganizationCommand organizationCommand = new OrganizationCommand();
        Account account = new Account(username, null);
        if (!TmcUtil.tryToLogin(ctx, account, password)) {
            return;
        }

        Optional<Organization> organization = organizationCommand.chooseOrganization(ctx, args);
        if (!organization.isPresent()) {
            return;
        }

        this.ctx.getSettings().setOrganization(organization);

        boolean sendDiagnostics = getSendDiagnosticsAnswer(username, account.getServerAddress());
        this.ctx.getSettings().setSendDiagnostics(sendDiagnostics);

        AccountList list = SettingsIo.loadAccountList();
        list.addAccount(account);
        if (!SettingsIo.saveAccountList(list)) {
            io.errorln("Failed to write the accounts file.");
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

    private boolean getSendDiagnosticsAnswer(String username, String server) {
        AccountList savedAccounts = SettingsIo.loadAccountList();
        if (savedAccounts.getAccount(username, server) != null) {
            // not the first time logging in, diagnostics not asked
            return this.ctx.getSettings().getSendDiagnostics();
        }
        while (true) {
            String sendDiagnostics = io.readLine("Do you want to send crash reports and diagnostics for client development? (y/n) ");
            if (sendDiagnostics.trim().toLowerCase().startsWith("y")) {
                return true;
            } else if (sendDiagnostics.trim().toLowerCase().startsWith("n")) {
                return false;
            }
            io.println("Please answer y(es) or n(o).");
        }
    }
}
