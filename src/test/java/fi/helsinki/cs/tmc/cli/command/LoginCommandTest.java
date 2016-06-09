package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import org.apache.http.entity.BasicHttpEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import static org.mockito.Matchers.any;
import org.powermock.api.easymock.PowerMock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SettingsIo.class)
public class LoginCommandTest {

    private static final String SERVER = "testserver";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";

    Application app;
    Io mockIo;
    TmcCore mockCore;

    @Before
    public void setUp() {
        // Unwanted behaviour? Will delete the real settings file atm.
        SettingsIo.delete();

        mockIo = mock(TerminalIo.class);
        app = new Application(mockIo);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
        app = Mockito.spy(app);
        when(app.getTmcCore()).thenReturn(mockCore);
    }

    @After
    public void tearDown() {
        // Unwanted behaviour? Will delete the real settings file atm.
        SettingsIo.delete();
    }

    @Test
    public void logsInWithCorrectServerUserAndPassword() {
        //PowerMock.mockStatic(SettingsIo.class);
        //when(SettingsIo.save(any(Settings.class))).thenReturn(true);

        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        when(SettingsIo.save(any(Settings.class))).thenReturn(true);
        String[] args = {"login", "-s", SERVER, "-u", USERNAME, "-p", PASSWORD};
        app.run(args);
        verify(mockIo).println(eq("Login succesful."));
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
        verify(mockIo).println(eq("Incorrect username or password."));
    }

    @Test
    public void loginAsksUsernameFromUser() {
        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        String[] args = {"login", "-s", SERVER, "-p", PASSWORD};
        when(mockIo.readLine("username: ")).thenReturn(USERNAME);
        app.run(args);
        verify(mockIo).readLine(eq("username: "));
    }

    @Test
    public void loginAsksPasswordFromUser() {
        when(mockCore.listCourses((ProgressObserver) anyObject()))
                .thenReturn(successfulCallable());
        String[] args = {"login", "-s", SERVER, "-u", USERNAME};
        when(mockIo.readPassword("password: ")).thenReturn(PASSWORD);
        app.run(args);
        verify(mockIo).readPassword(eq("password: "));
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
