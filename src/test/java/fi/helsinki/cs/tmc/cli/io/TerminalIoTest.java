package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TerminalIo.class, Console.class, System.class})
public class TerminalIoTest {

    private Io io;
    private OutputStream os;
    private InputStream oldInputStream;

    @Before
    public void setUp() throws Exception {
        oldInputStream = System.in;
        io = new TerminalIo();
        os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        System.setOut(ps);

        spy(System.class);
    }

    @After
    public void cleanUp() {
        System.setIn(oldInputStream);
    }

    public void writeString(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        System.setIn(new ByteArrayInputStream(bytes));
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

    @Test
    public void readLine() {
        writeString("test");
        assertEquals("test", io.readLine("abc "));
        assertEquals("abc ", os.toString());
    }

    @Test
    public void readLineIgnoresRestOfTheLines() {
        writeString("test\nawugwufgv");
        assertEquals("test", io.readLine("abc "));
        assertEquals("abc ", os.toString());
    }

    @Test
    public void readPassword() {
        Console mockConsole = mock(Console.class);
        when(mockConsole.readPassword(anyString())).thenReturn("password".toCharArray());
        when(System.console()).thenReturn(mockConsole);
        assertEquals("password", io.readPassword("abc "));
    }

    @Test
    public void failToReadPasswordBecauseException() {
        Console mockConsole = mock(Console.class);
        when(mockConsole.readPassword(anyString())).thenThrow(new RuntimeException());
        writeString("password");
        when(System.console()).thenReturn(mockConsole);
        assertEquals("password", io.readPassword("abc "));
        assertThat(os.toString(),
                containsString("Unable to read password"));
    }

    @Test
    public void failToReadPasswordBecauseConsoleIsNull() {
        when(System.console()).thenReturn(null);
        writeString("password");
        assertEquals("password", io.readPassword("abc "));
        assertThat(os.toString(),
                containsString("Unable to read password"));
    }

    @Test
    public void printConfimationDialogWhenDefaultIsYes() {
        writeString("\n");
        io.readConfirmation("abc", true);
        assertEquals("abc [Y/n] ", os.toString());
    }

    @Test
    public void printConfimationDialogWhenDefaultIsNo() {
        writeString("\n");
        io.readConfirmation("abc", false);
        assertEquals("abc [y/N] ", os.toString());
    }

    @Test
    public void readUppercaseConfirmation() {
        writeString("YES");
        assertEquals(true, io.readConfirmation("abc ", false));
    }

    @Test
    public void readYesConfirmation() {
        writeString("yes");
        assertEquals(true, io.readConfirmation("abc ", false));
    }

    @Test
    public void readYConfirmation() {
        writeString("y");
        assertEquals(true, io.readConfirmation("abc ", false));
    }

    @Test
    public void readNoConfirmation() {
        writeString("no");
        assertEquals(false, io.readConfirmation("abc ", true));
    }

    @Test
    public void readNConfirmation() {
        writeString("n");
        assertEquals(false, io.readConfirmation("abc ", true));
    }

    @Test
    public void readUnknownConfirmationAsDefaultYes() {
        writeString("def");
        assertEquals(true, io.readConfirmation("abc ", true));
    }

    @Test
    public void readUnknownConfirmationAsDefaultNo() {
        writeString("def");
        assertEquals(false, io.readConfirmation("abc ", false));
    }

    @Test
    public void readEmptyConfirmationAsDefaultYes() {
        writeString("\n");
        assertEquals(true, io.readConfirmation("abc ", true));
    }

    @Test
    public void readEmptyConfirmationAsDefaultNo() {
        writeString("\n");
        assertEquals(false, io.readConfirmation("abc ", false));
    }
}
