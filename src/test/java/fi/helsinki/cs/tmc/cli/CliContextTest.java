package fi.helsinki.cs.tmc.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CliContextTest {

    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
    }

    @Test
    public void getIoAfterItsSetInConstructor() {
        Io io = new TestIo();
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


    @Ignore
    @Test
    public void backendInitWithoutCourse() {
        CliContext ctx = new CliContext(null);
        assertTrue(ctx.loadBackend());
        assertEquals(null, ctx.getCourseInfo());
    }

    @Ignore
    @Test
    public void backendInitWithCourse() {
        CliContext ctx = new CliContext(null);
        assertTrue(ctx.loadBackend());
        assertNotSame(null, ctx.getCourseInfo());
    }

    @Ignore
    @Test
    public void backendInitWithInternet() {
        CliContext ctx = new CliContext(null);
        assertTrue(ctx.loadBackend());
        assertEquals(true, ctx.hasLogin());
    }

    @Ignore
    @Test
    public void backendInitWithoutInternet() {
        CliContext ctx = new CliContext(null);
        assertTrue(ctx.loadBackendWithoutLogin());
        assertEquals(false, ctx.hasLogin());
    }
}
