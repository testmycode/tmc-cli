package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class TerminalIoTest {

    Io io = new TerminalIo();
    OutputStream os;

    @Before
    public void setUp() {
        os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);
    }

    @Test
    public void printlnWorksRight() {
        io.println("foo");
        assertTrue(os.toString().equals("foo\n"));
    }
}
