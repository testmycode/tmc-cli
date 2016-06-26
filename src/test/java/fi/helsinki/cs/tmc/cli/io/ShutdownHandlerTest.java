package fi.helsinki.cs.tmc.cli.io;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ShutdownHandler.class)
public class ShutdownHandlerTest {

    private TestIo io;
    private ShutdownHandler shutdownHandler;
    private Runtime mockedRuntime;

    @Before
    public void setUp() {
        io = new TestIo();
        shutdownHandler = new ShutdownHandler(io);
        mockStatic(Runtime.class);

        mockedRuntime = mock(Runtime.class);
        when(Runtime.getRuntime()).thenReturn(mockedRuntime);
    }

    @Test
    public void printsAnsiResetAtRun() {
        shutdownHandler.run();
        io.assertEquals(Color.AnsiColor.ANSI_RESET.toString() + "\n");
    }

    @Test
    public void enableShutdownHook() {
        shutdownHandler.enable();
        verify(mockedRuntime, times(1)).addShutdownHook(shutdownHandler);
    }

    @Test
    public void disableShutdownHook() {
        shutdownHandler.disable();
        verify(mockedRuntime, times(1)).removeShutdownHook(shutdownHandler);
    }
}
