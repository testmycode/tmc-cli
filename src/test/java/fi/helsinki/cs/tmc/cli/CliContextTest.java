package fi.helsinki.cs.tmc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CourseInfoIo.class, SettingsIo.class})
public class CliContextTest {

    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
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
    public void backendInitWithoutCourse() {
        mockStatic(CourseInfoIo.class);

        CourseInfo info = null;
        WorkDir workDir = mock(WorkDir.class);

        when(workDir.getConfigFile()).thenReturn(null);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackend());
        assertEquals(null, ctx.getCourseInfo());
    }

    @Test
    public void backendInitWithCourse() {
        mockStatic(CourseInfoIo.class);

        CourseInfo info = mock(CourseInfo.class);
        WorkDir workDir = mock(WorkDir.class);
        Path path = mock(Path.class);

        when(CourseInfoIo.load(eq(path))).thenReturn(info);
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackend());
        assertEquals(info, ctx.getCourseInfo());
    }

    @Test
    public void backendInitWithInternet() {
        mockStatic(CourseInfoIo.class);
        mockStatic(SettingsIo.class);

        CourseInfo info = mock(CourseInfo.class);
        WorkDir workDir = mock(WorkDir.class);
        Path path = mock(Path.class);

        when(CourseInfoIo.load(eq(path))).thenReturn(info);
        when(SettingsIo.load(anyString(), anyString())).thenReturn(new Settings());
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackend());
        assertEquals(true, ctx.hasLogin());
    }

    @Test
    public void failBackendInitWithInternetButWithoutCourse() {
        mockStatic(CourseInfoIo.class);
        mockStatic(SettingsIo.class);

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
        mockStatic(SettingsIo.class);

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
        mockStatic(SettingsIo.class);

        WorkDir workDir = mock(WorkDir.class);
        when(workDir.getConfigFile()).thenReturn(null);
        when(SettingsIo.load()).thenReturn(null);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackendWithoutLogin());
        assertEquals(false, ctx.hasLogin());
    }

    @Test
    public void backendInitWithoutInternetWithCourse() {
        mockStatic(CourseInfoIo.class);
        mockStatic(SettingsIo.class);

        WorkDir workDir = mock(WorkDir.class);
        CourseInfo info = mock(CourseInfo.class);
        Path path = mock(Path.class);

        when(CourseInfoIo.load(eq(path))).thenReturn(info);
        when(SettingsIo.load(anyString(), anyString())).thenReturn(new Settings());
        when(workDir.getConfigFile()).thenReturn(path);
        CliContext ctx = new CliContext(io, null, workDir);

        assertTrue(ctx.loadBackendWithoutLogin());
        assertEquals(true, ctx.hasLogin());
    }
}
