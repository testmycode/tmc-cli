package fi.helsinki.cs.tmc.cli;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CliContextTest {

    @Before
    public void setUp() {
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
        CliContext ctx = new CliContext(null);
        Application app = new Application(ctx);
        ctx.setApp(app);
        assertEquals(app, ctx.getApp());
    }

    @Test(expected=RuntimeException.class)
    public void getAppWithoutSettingIt() {
        CliContext ctx = new CliContext(null);
        assertNull(ctx.getApp());
    }

    @Test
    public void setGetWorkDir() {
        WorkDir workDir = new WorkDir();
        CliContext ctx = new CliContext(null, null, workDir);
        assertEquals(workDir, ctx.getWorkDir());
    }


    @Ignore
    @Test
    public void backendInitWithoutCourse() {
    }

    @Ignore
    @Test
    public void backendInitWithCourse() {
    }
}
