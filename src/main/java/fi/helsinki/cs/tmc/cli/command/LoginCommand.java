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

        if (this.ctx.checkIsLoggedIn(true, true)) {
            Optional<String> username = this.ctx.getSettings().getUsername();
            Optional<Organization> organization = this.ctx.getSettings().getOrganization();
            io.println("You are already logged in " + (username.isPresent()? "as " + username.get() : "") +
                    (organization.isPresent() ?
                    " and your current organization is " + organization.get().getName() :
                    "."));
            io.println("You can change your organization with the command organization.");
            io.println("You can change your current settings with the command config.");
            return;
        }

        if (!TmcUtil.hasConnection(ctx)) {
            io.errorln("You don't have internet connection currently.");
            io.errorln("Check the tmc-cli logs if you disagree.");
            return;
        }

        CourseInfo info = ctx.getCourseInfo();
        if (info != null) {
            username = info.getUsername();
        }

        login(this.ctx, args, Optional.absent());
    }

    public boolean login(CliContext ctx, CommandLine args, Optional<String> serverAddress) {
        Io io = ctx.getIo();
        if (serverAddress.isPresent()) {
            io.println("Logging in to " + serverAddress.get());
        }
        username = getLoginInfo(args, username, "u", "username: ", io);
        password = getLoginInfo(args, null, "p", "password: ", io);


        Account account = new Account(username);
        if (serverAddress.isPresent()) {
            account.setServerAddress(serverAddress.get());
        }
        ctx.useAccount(account);

        if (!TmcUtil.tryToLogin(ctx, account, password)) {
            ctx.getSettings().setAccount(ctx, new Account());
            username = null;
            return false;
        }

        OrganizationCommand organizationCommand = new OrganizationCommand();
        Optional<Organization> organization = organizationCommand.chooseOrganization(ctx, Optional.fromNullable(args));
        if (!organization.isPresent()) {
            return false;
        }
        account.setOrganization(organization);

        boolean sendDiagnostics = getBooleanAnswerFromUser(Optional.fromNullable(username),
                "Do you want to send crash reports for client development?",
                                    ctx.getSettings().getSendDiagnostics(), io);
        account.setSendDiagnostics(sendDiagnostics);
        boolean sendAnalytics = getBooleanAnswerFromUser(Optional.fromNullable(username),
                "Do you want to send analytics data for research?",
                                    ctx.getSettings().isSpywareEnabled(), io);
        account.setSendAnalytics(sendAnalytics);

        AccountList list = SettingsIo.loadAccountList();
        list.addAccount(account);
        if (!SettingsIo.saveAccountList(list)) {
            io.errorln("Failed to write the accounts file.");
            return false;
        }

        ctx.getAnalyticsFacade().saveAnalytics("login");

        io.println("Login successful.");
        io.println("You can change your organization with the command organization, " +
                "and change other settings with the command config.");
        return true;
    }

    private String getLoginInfo(CommandLine line, String oldValue, String option, String prompt, Io io) {
        String value = oldValue;
        boolean isPassword = option.equals("p");

        if (line != null && line.hasOption(option)) {
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

    public boolean getBooleanAnswerFromUser(Optional<String> username, String prompt, boolean defaultValue, Io io) {
        AccountList savedAccounts = SettingsIo.loadAccountList();
        if (username.isPresent() && savedAccounts.getAccount(username.get()) != null) {
            // not the first time logging in
            return defaultValue;
        }
        while (true) {
            String sendInfo = io.readLine(prompt + " (Y/n) ");
            String answer = sendInfo.trim().toLowerCase();
            if (answer.isEmpty() || answer.startsWith("y")) {
                io.println("Set to yes");
                return true;
            } else if (answer.startsWith("n")) {
                io.println("Set to no");
                return false;
            }
            io.println("Please answer y(es) or n(o).");
        }
    }
}
