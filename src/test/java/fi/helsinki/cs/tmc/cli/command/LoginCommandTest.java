package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LoginCommandTest {

    private static String serverAddress;
    private static String username;
    private static String password;

    private Application app;
    Io mockIo;

    @BeforeClass
    public static void setUpClass() {
        serverAddress = System.getenv("TMC_SERVER_ADDRESS");
        username = System.getenv("TMC_USERNAME");
        password = System.getenv("TMC_PASSWORD");
        
        assertNotNull(serverAddress);
        assertNotNull(username);
        assertNotNull(password);
    }

    @Before
    public void setUp() {
        // Unwanted behaviour? Will delete the real settings file atm.
        new SettingsIo().delete();
        mockIo = mock(TerminalIo.class);
        app = new Application(mockIo);
    }

    @After
    public void tearDown() {
        // Unwanted behaviour? Will delete the real settings file atm.
        new SettingsIo().delete();
    }

    @Test
    public void logsInWithCorrectServerUserAndPassword() {
        String[] args = {"login", "-s", serverAddress, "-u", username, "-p", password};
        app.run(args);
        verify(mockIo).println(eq("Login succesful."));
    }

    @Test
    public void catches401IfCorrectServerAndWrongUsername() {
        String[] args = {"login", "-s", serverAddress, "-u", "foo", "-p", password};
        app.run(args);
        verify(mockIo).println(eq("Incorrect username or password."));
    }
    
    @Test
    public void loginAsksUsernameFromUser() {
        String[] args = {"login", "-s", serverAddress, "-p", password};
        when(mockIo.readLine("username: ")).thenReturn(username);
        app.run(args);
        verify(mockIo).readLine(eq("username: "));
    }
    
    @Test
    public void loginAsksPasswordFromUser() {
        String[] args = {"login", "-s", serverAddress, "-u", username};
        when(mockIo.readPassword("password: ")).thenReturn(password);
        app.run(args);
        verify(mockIo).readPassword(eq("password: "));
    }
}
