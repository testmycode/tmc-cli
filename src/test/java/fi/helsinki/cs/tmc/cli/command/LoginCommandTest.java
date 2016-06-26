package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import org.apache.http.entity.BasicHttpEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.rmi.ServerException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SettingsIo.class, TmcUtil.class})
public class LoginCommandTest {

    private static final String SERVER = "testserver";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";

    Application app;
    TestIo io;
    TmcCore mockCore;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);

        mockStatic(TmcUtil.class);
        mockStatic(SettingsIo.class);
        when(SettingsIo.save(any(Settings.class))).thenReturn(true);
    }

    @Test
    public void logsInWithCorrectServerUserAndPassword() throws Exception {
        when(TmcUtil.tryToLogin(mockCore)).thenReturn(true);
        when(SettingsIo.save(any(Settings.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME, "-p", PASSWORD};
        app.run(args);
        io.assertContains("Login successful.");
    }
    
    @Test
    public void userGetsErrorMessageIfLoginFails() throws Exception {
        when(TmcUtil.tryToLogin(mockCore)).thenReturn(true);
        when(SettingsIo.save(any(Settings.class))).thenReturn(false);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME, "-p", "WrongPassword"};
        app.run(args);
        io.assertContains("Login failed.");
    }

    @Test
    public void catches401IfCorrectServerAndWrongUsername() throws Exception {
        Exception cause = new FailedHttpResponseException(401, new BasicHttpEntity());
        Exception exception = new Exception(cause);
        when(TmcUtil.tryToLogin(any(TmcCore.class))).thenThrow(exception);
        String[] args = {"login", "-s", SERVER, "-u", "foo", "-p", PASSWORD};
        app.run(args);
        io.assertContains("Incorrect username or password.");
    }
    
    @Test
    public void userGetsErrorMessageIfUnableToConnectToServer() throws Exception {
        when(TmcUtil.tryToLogin(any(TmcCore.class))).thenThrow(new ServerException("Error"));
        String[] args = {"login", "-s", SERVER, "-u", "foo", "-p", PASSWORD};
        app.run(args);
        io.assertContains("Unable to connect to server");
    }

    @Test
    public void loginAsksUsernameFromUserIfNotGiven() throws Exception {
        when(TmcUtil.tryToLogin(any(TmcCore.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-p", PASSWORD};
        io.addLinePrompt(USERNAME);
        app.run(args);
        io.assertAllPromptsUsed();
    }

    @Test
    public void loginAsksPasswordFromUserIfNotGiven() throws Exception {
        when(TmcUtil.tryToLogin(any(TmcCore.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME};
        io.addPasswordPrompt(PASSWORD);
        app.run(args);
        io.assertAllPromptsUsed();
    }
    
    @Test
    public void loginAsksServerFromUserIfNotGiven() throws Exception {
        when(TmcUtil.tryToLogin(any(TmcCore.class))).thenReturn(true);
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME};
        io.addLinePrompt(SERVER);
        app.run(args);
        io.assertAllPromptsUsed();
    }
}
