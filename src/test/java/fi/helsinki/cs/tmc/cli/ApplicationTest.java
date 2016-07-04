package fi.helsinki.cs.tmc.cli;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.updater.TmcCliUpdater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcCliUpdater.class)
public class ApplicationTest {

    private Application app;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(new CliContext(io));
        mockStatic(TmcCliUpdater.class);
    }

    @Test
    public void versionWorksWithRightParameter() {
        String[] args = {"-v", "foo"};
        app.run(args);
        assertTrue(io.out().contains("TMC-CLI version"));
        io.assertNotContains("Command foo doesn't exist.");
    }

    @Test
    public void failWhenInvalidOption() {
        String[] args = {"-a34t3"};
        app.run(args);
        io.assertContains("Unrecognized option");
    }

    @Test
    public void helpWorksWithRightParameter() {
        String[] args = {"-h"};
        app.run(args);
        io.assertContains("Usage: tmc");
    }

    @Test
    public void helpOfHelpCommandIsNotGiven() {
        String[] args = {"-h", "help"};
        app.run(args);
        io.assertContains("help");
        io.assertNotContains("Usage: tmc help");
    }

    @Test
    public void runCommandWorksWithWrongParameter() {
        String[] args = {"foo"};
        app.run(args);
        io.assertContains("Command foo doesn't exist");
    }

    @Test
    public void runAutoUpdate() {
        String[] args = {"help"};
        TmcCliUpdater mockUpdater = mock(TmcCliUpdater.class);
        when(TmcCliUpdater.createUpdater(any(Io.class), anyString(), any(Boolean.class)))
                .thenReturn(mockUpdater);
        when(mockUpdater.run()).thenReturn(true);

        CliContext ctx = spy(new CliContext(null));
        when(ctx.getProperties()).thenReturn(new HashMap<String, String>());

        app = new Application(ctx);
        app.run(args);

        verifyStatic(times(1));
        TmcCliUpdater.createUpdater(any(Io.class), anyString(), any(Boolean.class));
    }

    @Test
    public void runWithForceUpdate() {
        String[] args = {"--force-update", "foo"};
        app = spy(app);
        doReturn(true).when(app).runAutoUpdate();
        app.run(args);
        io.assertNotContains("Command foo doesn't exist.");
        verify(app, times(1)).runAutoUpdate();
    }
}
