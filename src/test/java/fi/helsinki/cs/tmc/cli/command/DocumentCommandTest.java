package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentUtil.class)
public class DocumentCommandTest {

    private Application app;
    private CliContext ctx;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        ctx = new CliContext(io);
        app = new Application(ctx);

        mockStatic(EnvironmentUtil.class);
        when(EnvironmentUtil.getTerminalWidth()).thenReturn(100);
    }

    @Test
    public void run() throws InterruptedException {
        when(EnvironmentUtil.isWindows()).thenReturn(false);
        app = new Application(ctx);

        String[] args = {"document", "-s", "0"};
        app.run(args);
        io.assertContains("Original dev team");
    }

    @Test
    public void failOnWindows() throws InterruptedException {
        when(EnvironmentUtil.isWindows()).thenReturn(true);
        app = new Application(ctx);

        String[] args = {"document", "-s", "0"};
        app.run(args);
        io.assertContains("Command document doesn't exist");
    }
}
