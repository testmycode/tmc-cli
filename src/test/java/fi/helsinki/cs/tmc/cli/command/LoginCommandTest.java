package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import org.apache.http.entity.BasicHttpEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SettingsIo.class)
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

        app = Mockito.spy(app);
        when(app.getTmcCore()).thenReturn(mockCore);

        PowerMockito.mockStatic(SettingsIo.class);
        when(SettingsIo.save(any(Settings.class))).thenReturn(true);
    }

    @Test
    public void logsInWithCorrectServerUserAndPassword() {
        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        when(SettingsIo.save(any(Settings.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME, "-p", PASSWORD};
        app.run(args);
        assertThat(io.out(), containsString("Login successful."));
    }
    
    @Test
    public void userGetsErrorMessageIfLoginFails() {
        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        when(SettingsIo.save(any(Settings.class))).thenReturn(false);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME, "-p", "WrongPassword"};
        app.run(args);
        assertThat(io.out(), containsString("Login failed."));
    }

    @Test
    public void catches401IfCorrectServerAndWrongUsername() {
        Callable<List<Course>> callable401 = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                throw new Exception(new FailedHttpResponseException(401, new BasicHttpEntity()));
            }
        };
        when(mockCore.listCourses((ProgressObserver) anyObject())).thenReturn(callable401);
        String[] args = {"login", "-s", SERVER, "-u", "foo", "-p", PASSWORD};
        app.run(args);
        assertThat(io.out(), containsString("Incorrect username or password."));
    }
    
    @Test
    public void userGetsErrorMessageIfUnableToConnectToServer() {
        Callable<List<Course>> callableEx = new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                throw new ServerException("SomeException");
            }
        };
        when(mockCore.listCourses((ProgressObserver) anyObject())).thenReturn(callableEx);
        String[] args = {"login", "-s", SERVER, "-u", "foo", "-p", PASSWORD};
        app.run(args);
        assertThat(io.out(), containsString("Unable to connect to server"));
    }

    @Test
    public void loginAsksUsernameFromUserIfNotGiven() {
        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        String[] args = {"login", "-s", SERVER, "-p", PASSWORD};
        io.addLinePrompt(USERNAME);
        app.run(args);
        assertTrue(io.allPromptsUsed());
    }

    @Test
    public void loginAsksPasswordFromUserIfNotGiven() {
        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        String[] args = {"login", "-s", SERVER, "-u", USERNAME};
        io.addPasswordPrompt(PASSWORD);
        app.run(args);
        assertTrue(io.allPromptsUsed());
    }
    
    @Test
    public void loginAsksServerFromUserIfNotGiven() {
        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        String[] args = {"login", "-p", PASSWORD, "-u", USERNAME};
        io.addLinePrompt(SERVER);
        app.run(args);
        assertTrue(io.allPromptsUsed());
    }

    private static Callable<List<Course>> successfulCallable() {
        return new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                return new ArrayList<>();
            }
        };
    }
}
