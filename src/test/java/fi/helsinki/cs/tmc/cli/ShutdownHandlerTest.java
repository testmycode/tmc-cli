package fi.helsinki.cs.tmc.cli;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;

import org.junit.Before;
import org.junit.Test;

public class ShutdownHandlerTest {

    private Io mockIo;
    private ShutdownHandler shutdownHandler;

    @Before
    public void setUp() {
        mockIo = mock(TerminalIo.class);
        shutdownHandler = new ShutdownHandler(mockIo);
    }

    @Test
    public void printsAnsiResetAtRun() {
        shutdownHandler.run();
        verify(mockIo).println(eq(Color.AnsiColor.ANSI_RESET.toString()));
    }
}
