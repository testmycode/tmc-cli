package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SettingsIo.class, TmcUtil.class})
public class LoginCommandTest {

    private static final String SERVER = "testserver";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;

    @Before
    public void setUp() {
        io = new TestIo();
        mockCore = mock(TmcCore.class);

        ctx = spy(new CliContext(io, mockCore));
        app = new Application(ctx);

        mockStatic(TmcUtil.class);
        mockStatic(SettingsIo.class);
        when(SettingsIo.save(any(Settings.class))).thenReturn(true);
    }

    @Test
    public void logsInWithCorrectServerUserAndPassword() throws Exception {
        when(TmcUtil.tryToLogin(eq(ctx), any(Settings.class))).thenReturn(true);
        when(SettingsIo.save(any(Settings.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME, "-p", PASSWORD};
        app.run(args);
        io.assertContains("Login successful.");
    }

    @Test
    public void userGetsErrorMessageIfLoginFails() throws Exception {
        when(TmcUtil.tryToLogin(eq(ctx), any(Settings.class))).thenReturn(true);
        when(SettingsIo.save(any(Settings.class))).thenReturn(false);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME, "-p", "WrongPassword"};
        app.run(args);
        io.assertContains("Login failed.");
    }

    @Test
    public void loginAsksUsernameFromUserIfNotGiven() throws Exception {
        when(TmcUtil.tryToLogin(eq(ctx), any(Settings.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-p", PASSWORD};
        io.addLinePrompt(USERNAME);
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void loginAsksPasswordFromUserIfNotGiven() throws Exception {
        when(TmcUtil.tryToLogin(eq(ctx), any(Settings.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME};
        io.addPasswordPrompt(PASSWORD);
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void loginAsksServerFromUserIfNotGiven() throws Exception {
        when(TmcUtil.tryToLogin(eq(ctx), any(Settings.class))).thenReturn(true);
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME};
        io.addLinePrompt(SERVER);
        app.run(args);
        io.assertAllPromptsUsed();
    }
}
