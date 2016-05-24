package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.Application;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class LoginCommandTest {

    private static String serverAddress;
    private static String username;
    private static String password;

    private Application app;
    private OutputStream os;

    public LoginCommandTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        LoginCommandTest.serverAddress = System.getenv("TMC_SERVER_ADDRESS");
        LoginCommandTest.username = System.getenv("TMC_USERNAME");
        LoginCommandTest.password = System.getenv("TMC_PASSWORD");
    }

    @Before
    public void setUp() {
        this.app = new Application();
        this.os = new ByteArrayOutputStream();

        PrintStream ps = new PrintStream(os);
        System.setOut(ps);
    }

    @Test
    public void logsInWithCorrectServerUserAndPassword() {
        String[] args = {"login",
            "-s", LoginCommandTest.serverAddress,
            "-u", LoginCommandTest.username,
            "-p", LoginCommandTest.password};

        app.run(args);
        String output = os.toString();
        assertTrue(output.contains("Login successful!"));
    }
}
