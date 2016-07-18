package fi.helsinki.cs.tmc.cli.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.nio.file.Path;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CourseInfoIo.class, SettingsIo.class})
public class CliContextTest {

    private TestIo io;
    private AccountList list;

    @Before
    public void setUp() {
        io = new TestIo();
        list = new AccountList();

        mockStatic(SettingsIo.class);
        when(SettingsIo.loadAccountList()).thenReturn(list);
    }

    @Test
    public void getIoAfterItsSetInConstructor() {
        CliContext ctx = new CliContext(io);
        assertEquals(io, ctx.getIo());
    }

    @Test
    public void getIoWhenItsNotGiven() {
        CliContext ctx = new CliContext(null);
        assertEquals(TerminalIo.class, ctx.getIo().getClass());
    }

    @Test
    public void setAppAndGetIt() {
        CliContext ctx = new CliContext(io);
        Application app = new Application(ctx);
        ctx.setApp(app);
        assertEquals(app, ctx.getApp());
    }

    @Test
    public void useDifferentAccount() {
        mockStatic(CourseInfoIo.class);

        WorkDir workDir = mock(WorkDir.class);
        Path path = mock(Path.class);
        Account newAccount = new Account();

        when(CourseInfoIo.load(eq(path))).thenReturn(mock(CourseInfo.class));
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        ctx.loadBackend();
        ctx.useAccount(newAccount);

        //TODO replace the Whitebox usage somehow
        Settings usedSettings = Whitebox.getInternalState(ctx, "settings");
        assertEquals(newAccount, usedSettings.getAccount());
    }

    @Test(expected = RuntimeException.class)
    public void getAppWithoutSettingIt() {
        CliContext ctx = new CliContext(io);
        assertNull(ctx.getApp());
    }

    @Test
    public void setGetWorkDir() {
        WorkDir workDir = new WorkDir();
        CliContext ctx = new CliContext(io, null, workDir);
        assertEquals(workDir, ctx.getWorkDir());
    }

    @Test
    public void getCourseInfo() {
        mockStatic(CourseInfoIo.class);

        CourseInfo info = null;
        WorkDir workDir = mock(WorkDir.class);

        when(workDir.getConfigFile()).thenReturn(null);
        CliContext ctx = new CliContext(io, null, workDir);

        assertEquals(null, ctx.getCourseInfo());
    }

    @Test
    public void getCourseInfoWhenItDoesntExist() {
        mockStatic(CourseInfoIo.class);

        CourseInfo info = mock(CourseInfo.class);
        WorkDir workDir = mock(WorkDir.class);
        Path path = mock(Path.class);

        when(CourseInfoIo.load(eq(path))).thenReturn(info);
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertEquals(info, ctx.getCourseInfo());
    }

    @Test
    public void backendInitWithInternet() {
        mockStatic(CourseInfoIo.class);

        CourseInfo info = mock(CourseInfo.class);
        WorkDir workDir = mock(WorkDir.class);
        Path path = mock(Path.class);
        list.addAccount(new Account());

        when(CourseInfoIo.load(eq(path))).thenReturn(info);
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackend());
        assertEquals(true, ctx.hasLogin());
    }

    @Test
    public void failBackendInitWithCourseButWithoutInternet() {
        mockStatic(CourseInfoIo.class);

        WorkDir workDir = mock(WorkDir.class);
        Path path = mock(Path.class);
        CourseInfo info = mock(CourseInfo.class);

        when(info.getUsername()).thenReturn("user");
        when(CourseInfoIo.load(eq(path))).thenReturn(info);
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertFalse(ctx.loadBackend());
        assertEquals(false, ctx.hasLogin());
        io.assertContains("You are not logged in as user. Log in using: tmc login");
    }

    @Test
    public void failBackendInitWithInternetButWithoutCourse() {
        mockStatic(CourseInfoIo.class);

        WorkDir workDir = mock(WorkDir.class);
        when(workDir.getConfigFile()).thenReturn(null);
        CliContext ctx = new CliContext(io, null, workDir);

        assertFalse(ctx.loadBackend());
        assertEquals(false, ctx.hasLogin());
        io.assertContains("You are not logged in");
    }

    @Test
    public void failBackendInitWithInternetButWithCorruptedCourse() {
        mockStatic(CourseInfoIo.class);

        WorkDir workDir = mock(WorkDir.class);
        Path path = mock(Path.class);

        when(CourseInfoIo.load(eq(path))).thenReturn(null);
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertFalse(ctx.loadBackend());
        assertEquals(false, ctx.hasLogin());
        io.assertContains("Course configuration file");
        io.assertContains("is invalid.");
        io.assertContains("You are not logged in");
    }

    @Test
    public void backendInitWithoutInternet() {
        WorkDir workDir = mock(WorkDir.class);
        when(workDir.getConfigFile()).thenReturn(null);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackendWithoutLogin());
        assertEquals(false, ctx.hasLogin());
    }

    @Test
    public void backendInitWithoutInternetWithCourse() {
        mockStatic(CourseInfoIo.class);

        WorkDir workDir = mock(WorkDir.class);
        CourseInfo info = mock(CourseInfo.class);
        Path path = mock(Path.class);
        list.addAccount(new Account());

        when(CourseInfoIo.load(eq(path))).thenReturn(info);
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackendWithoutLogin());
        assertEquals(true, ctx.hasLogin());
    }
}
