package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;

import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.snapshots.EventSendBuffer;
import fi.helsinki.cs.tmc.snapshots.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SettingsIo.class, TmcUtil.class})
public class LoginCommandTest {

    private static final String SERVER = "testserver";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String ORGANIZATION = "test";
    private static List<Organization> organizationList;
    private static final Organization TEST_ORGANIZATION = new Organization("test", "test", "test", "test", false);


    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;

    @Before
    public void setUp() {
        io = new TestIo();
        organizationList = new ArrayList<>();
        organizationList.add(new Organization("Test", "", "test", "", false));
        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        mockCore = new TmcCore(settings, tmcLangs);
        EventSendBuffer eventSendBuffer = new EventSendBuffer(new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(eventSendBuffer);

        ctx = spy(new CliContext(io, mockCore, new WorkDir(), settings, analyticsFacade));
        app = new Application(ctx);

        mockStatic(TmcUtil.class);
        mockStatic(SettingsIo.class);
        when(TmcUtil.hasConnection(eq(ctx))).thenReturn(true);
        when(SettingsIo.loadAccountList()).thenReturn(new AccountList());
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(true);
    }


    @Test
    public void failIfThereIsNoConnection() {
        when(TmcUtil.hasConnection(eq(ctx))).thenReturn(false);

        String[] args = {"login"};
        app.run(args);
        io.assertContains("don't have internet connection");
    }

    @Test
    public void logsInWithCorrectServerUserPasswordAndOrganization() {
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(true);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        String[] args = {"login", "-u", USERNAME, "-p", PASSWORD, "-o", ORGANIZATION};
        io.addLinePrompt("y");
        io.addLinePrompt("y");
        app.run(args);
        io.assertContains("Login successful.");
    }

    @Test
    public void userGetsErrorMessageIfLoginFails() {
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(false);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        String[] args = {"login", "-u", USERNAME, "-p", PASSWORD, "-o", ORGANIZATION};
        io.addLinePrompt("y");
        io.addLinePrompt("y");
        app.run(args);
        io.assertContains("Failed to write the accounts file.");
    }

    @Test
    public void loginAsksUsernameFromUserIfNotGiven() {
        when(SettingsIo.loadAccountList()).thenReturn(new AccountList());
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(true);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        String[] args = {"login", "-p", PASSWORD, "-o", ORGANIZATION};
        io.addLinePrompt(USERNAME);
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void loginAsksPasswordFromUserIfNotGiven() {
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        String[] args = {"login", "-u", USERNAME, "-o", ORGANIZATION};
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        io.addPasswordPrompt(PASSWORD);
        io.addLinePrompt("y");
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void loginAsksServerFromUserIfNotGiven() {
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME, "-o", ORGANIZATION};
        io.addLinePrompt(SERVER);
        io.addLinePrompt("y");
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void serverAndNotAskedAfterLogout() {
        Account account = new Account("username", "pass", TEST_ORGANIZATION);
        CourseInfo info = new CourseInfo(account, null);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        when(ctx.getCourseInfo()).thenReturn(info);
        String[] args = {"login"};
        io.addPasswordPrompt(PASSWORD);
        io.addLinePrompt(ORGANIZATION);
        io.addLinePrompt("y");
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void courseInfoValuesOverridedByOptions() {
        Account account = new Account("username", "pass", TEST_ORGANIZATION);
        CourseInfo info = new CourseInfo(account, null);
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        when(ctx.getCourseInfo()).thenReturn(info);
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME, "-o", TEST_ORGANIZATION.getSlug()};
        io.addLinePrompt("y");
        app.run(args);
        io.assertAllPromptsUsed();

        Account expectedAccount = new Account(USERNAME);
        verifyStatic();
        TmcUtil.tryToLogin(eq(ctx), eq(expectedAccount), eq(PASSWORD));
    }

    @Test
    public void organizationIsChosenOnLogin() {
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        String[] args = {"login"};
        io.addLinePrompt(USERNAME);
        io.addPasswordPrompt(PASSWORD);
        io.addLinePrompt(ORGANIZATION);
        io.addLinePrompt("y");
        app.run(args);
        io.assertContains("Choose an organization");
    }

    @Test
    public void organizationAskedFromUserIfNotGiven() {
        when(SettingsIo.loadAccountList()).thenReturn(new AccountList());
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(true);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME};
        io.addLinePrompt(ORGANIZATION);
        io.addLinePrompt("y");
        app.run(args);
        io.assertAllPromptsUsed();
    }


    @Test
    public void organizationNotChosenIfUsernameOrPasswordIncorrect() {
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(false);
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME};
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void organizationOfAccountNotNullAfterLoggingIn() {
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(false);
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME};
        app.run(args);
        assertTrue(ctx.getSettings().getAccount().getOrganization() != null);
    }

    @Test
    public void diagnosticsAskedOnFirstLogin() {
        when(SettingsIo.loadAccountList()).thenReturn(new AccountList());
        when(TmcUtil.tryToLogin(eq(ctx), any(Account.class), eq(PASSWORD))).thenReturn(true);
        when(TmcUtil.getOrganizationsFromServer(any(CliContext.class))).thenReturn(organizationList);
        String[] args = {"login", "-u", USERNAME, "-p", PASSWORD, "-o", TEST_ORGANIZATION.getSlug()};
        io.addLinePrompt("y");
        app.run(args);
        io.assertContains("want to send crash reports");
    }

    @Test
    public void logInNotAllowedIfAlreadyLoggedIn() {
        AccountList list = new AccountList();
        list.addAccount(new Account("loggedin"));
        when(SettingsIo.loadAccountList()).thenReturn(list);
        String[] args = {"login"};
        app.run(args);
        io.assertContains("You are already logged in");
    }
}
