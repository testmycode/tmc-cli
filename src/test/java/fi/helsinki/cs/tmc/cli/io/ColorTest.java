package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentUtil.class)
public class ColorTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(EnvironmentUtil.class);
    }

    @Test
    public void colorsWorkInNonWindows() {
        when(EnvironmentUtil.isWindows()).thenReturn(false);
        String string = Color.colorString("foobar", Color.AnsiColor.ANSI_BLACK);
        assertEquals("\u001B[30mfoobar\u001B[0m", string);
    }

    @Test
    public void colorsWorkInWindows() {
        when(EnvironmentUtil.isWindows()).thenReturn(true);
        String string = Color.colorString("foobar", Color.AnsiColor.ANSI_BLACK);
        assertEquals("foobar", string);
    }

    @Test
    public void noColorWorks() {
        String string = Color.colorString("foobar", Color.AnsiColor.ANSI_NONE);
        assertEquals("foobar", string);
    }
}
