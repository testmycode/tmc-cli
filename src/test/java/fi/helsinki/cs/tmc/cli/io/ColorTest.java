package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentUtil.class)
public class ColorTest {

    @Before
    public void setup() {
        mockStatic(EnvironmentUtil.class);
    }

    @Test
    public void colorsWorkInNonWindows() {
        when(EnvironmentUtil.isWindows()).thenReturn(false);
        String string = ColorUtil.colorString("foobar", Color.BLACK);
        assertEquals("\u001B[30mfoobar\u001B[0m", string);
    }

    @Test
    public void colorsWorkInWindows() {
        when(EnvironmentUtil.isWindows()).thenReturn(true);
        String string = ColorUtil.colorString("foobar", Color.BLACK);
        assertEquals("foobar", string);
    }

    @Test
    public void noColorWorks() {
        String string = ColorUtil.colorString("foobar", Color.NONE);
        assertEquals("foobar", string);
    }

    @Test
    public void getLowerCaseGreenColor() {
        Color color = ColorUtil.getColor("green");
        assertEquals(Color.GREEN, color);
    }

    @Test
    public void getCamelCaseGreenColor() {
        Color color = ColorUtil.getColor("Green");
        assertEquals(Color.GREEN, color);
    }

    @Test
    public void getUpperCaseGreenColor() {
        Color color = ColorUtil.getColor("GREEN");
        assertEquals(Color.GREEN, color);
    }

    @Test
    public void getGreenWithSpecialCharColor() {
        Color color = ColorUtil.getColor("green$");
        assertEquals(null, color);
    }

    @Test
    public void getInvalidColor() {
        Color color = ColorUtil.getColor("xgrewsg");
        assertEquals(null, color);
    }

    @Test
    public void getResetColor() {
        Color color = ColorUtil.getColor("reset");
        assertEquals(null, color);
    }

    @Test
    public void getNullAsColor() {
        Color color = ColorUtil.getColor(null);
        assertEquals(null, color);
    }
}
