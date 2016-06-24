package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ColorTest {

    private Boolean noColor;

    @Before
    public void setup() {
        this.noColor = EnvironmentUtil.isWindows();
    }

    @Test
    public void colorsWork() {
        String string = Color.colorString("foobar", Color.AnsiColor.ANSI_BLACK);
        if (!noColor) {
            assertEquals("\u001B[30mfoobar\u001B[0m", string);
        } else {
            assertEquals("foobar", string);
        }
    }

    @Test
    public void noColorWorks() {
        String string = Color.colorString("foobar", Color.AnsiColor.ANSI_NONE);
        assertEquals("foobar", string);
    }
}
