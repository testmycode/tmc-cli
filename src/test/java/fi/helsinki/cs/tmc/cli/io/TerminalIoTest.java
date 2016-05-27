package fi.helsinki.cs.tmc.cli.io;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class TerminalIoTest {

    Io mockIo;

    @Before
    public void setUp() {
        mockIo = mock(TerminalIo.class);
    }

    @Test
    public void printIsCalledWithRightParameter() {
        mockIo.print("foo");
        verify(mockIo).print(eq("foo"));
    }

    @Test
    public void readLineIsCalledWithRightParameter() {
        mockIo.readLine("prompt");
        verify(mockIo).readLine(eq("prompt"));
    }

    @Test
    public void readPasswordIsCalledWithRightParameter() {
        mockIo.readPassword("prompt");
        verify(mockIo).readPassword(eq("prompt"));
    }
}
