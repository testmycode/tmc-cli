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
            io.println("You are already logged in as " + this.ctx.getSettings().getUsername().get() +
                    (this.ctx.getSettings().getOrganization().isPresent() ?
                    " and your current organization is " + this.ctx.getSettings().getOrganization().get().getName() :
                    "."));
            io.println("Change your organization with the command organization.");
            io.println("Inspect and change your current settings with the command config.");
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

        username = getLoginInfo(args, username, "u", "username: ");
        password = getLoginInfo(args, null, "p", "password: ");

        Account account = new Account(username);
        ctx.useAccount(account);

        if (!TmcUtil.tryToLogin(ctx, account, password)) {
            this.ctx.getSettings().setAccount(this.ctx, new Account());
            return;
        }

        OrganizationCommand organizationCommand = new OrganizationCommand();
        Optional<Organization> organization = organizationCommand.chooseOrganization(ctx, args);
        if (!organization.isPresent()) {
            return;
        }
        account.setOrganization(organization);

        boolean sendDiagnostics = getAnswerFromUser(username, account.getServerAddress(),
                            "Do you want to send crash reports for client development?",
                                    this.ctx.getSettings().getSendDiagnostics(), this.io);
        account.setSendDiagnostics(sendDiagnostics);
        boolean sendAnalytics = getAnswerFromUser(username, account.getServerAddress(),
                            "Do you want to send analytics data for research?",
                                    this.ctx.getSettings().isSpywareEnabled(), this.io);
        account.setSendAnalytics(sendAnalytics);

        AccountList list = SettingsIo.loadAccountList();
        list.addAccount(account);
        if (!SettingsIo.saveAccountList(list)) {
            io.errorln("Failed to write the accounts file.");
            return;
        }

        this.ctx.getAnalyticsFacade().saveAnalytics("login");

        io.println("Login successful.");
        io.println("You can change your organization with the command organization, " +
                "and inspect and change other settings with the command config.");
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

    public boolean getAnswerFromUser(String username, String server, String prompt, boolean defaultValue, Io io) {
        AccountList savedAccounts = SettingsIo.loadAccountList();
        if (username != null && savedAccounts.getAccount(username) != null) {
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
