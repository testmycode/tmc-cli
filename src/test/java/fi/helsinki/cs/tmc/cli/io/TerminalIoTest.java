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
    public void printWord() {
        io.print("foo");
        assertTrue(os.toString().equals("foo"));
    }

    @Test
    public void printLineWithNewLine() {
        io.println("foo");
        assertTrue(os.toString().equals("foo\n"));
    }

    @Test
    public void printNull() {
        io.print(null);
        assertTrue(os.toString().equals("null"));
    }

    @Test
    public void printInteger() {
        io.print(5);
        assertTrue(os.toString().equals("5"));
    }
}
