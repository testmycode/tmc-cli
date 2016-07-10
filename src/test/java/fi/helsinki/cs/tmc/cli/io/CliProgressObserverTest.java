package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentUtil.class)
public class CliProgressObserverTest {

    private TestIo io;

    @Before
    public void setup() {
        this.io = new TestIo();
        mockStatic(EnvironmentUtil.class);
        when(EnvironmentUtil.getTerminalWidth()).thenReturn(50);
    }

    @Test
    public void progressMessageWorks() {
        CliProgressObserver progobs = new CliProgressObserver(io);
        progobs.progress(0, "Hello, world!");
        assertTrue("Prints message", io.out().contains(
                "Hello, world!                                     "));
    }

    @Test
    public void progressBarWorks() {
        CliProgressObserver progobs = new CliProgressObserver(io);
        progobs.progress(0, 0.5, "Hello, world!");
        assertTrue("Prints message", io.out().contains("Hello, world!"));
        assertTrue("Prints the start of the progress bar", io.out().contains(
                " 50%["));
        assertTrue("Prints the first part of the progress bar", io.out().contains(
                "██████████████████████"));
        assertTrue("Prints the second of the progress bar", io.out().contains(
                "░░░░░░░░░░░░░░░░░░░░░░"));
        assertTrue("Prints the end of the progress bar", io.out().contains(
                "]"));
    }

    @Test
    public void testResultBarWorks() {
        String string = CliProgressObserver.getPassedTestsBar(1, 2,
                Color.NONE, Color.NONE);
        assertTrue("Prints the start of the progress bar", string.contains(
                " 50%["));
        assertTrue("Prints the first part of the progress bar", string.contains(
                "██████████████████████"));
        assertTrue("Prints the second of the progress bar", string.contains(
                "░░░░░░░░░░░░░░░░░░░░░░"));
        assertTrue("Prints the end of the progress bar", string.contains(
                "]"));
    }

    @Test
    public void shortensLongMessages() {
        CliProgressObserver progobs = new CliProgressObserver(io);
        progobs.progress(0,
                "fooooooooooooooooooooooooooooooooooooooooooooooooobar");
        assertTrue("Prints what it's supposed to",
                io.out().contains("foooooooooooooooooooooooooooooooooooooooooooooo..."));
        assertTrue("Doesn't print what it's not supposed to",
                !io.out().contains("bar"));
    }

    @Test
    public void percentagesWork() {
        assertEquals("  6%", CliProgressObserver.percentage(0.06));
        assertEquals(" 20%", CliProgressObserver.percentage(0.2));
        assertEquals("100%", CliProgressObserver.percentage(1.0));
    }
}
