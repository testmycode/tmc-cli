package fi.helsinki.cs.tmc.cli.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentUtil.class)
public class EnvironmentUtilTest {

    @Before
    public void setUp() {
        PowerMockito.mockStatic(System.class);
        PowerMockito.mockStatic(Class.class);
    }

    @Test
    public void detectsNonWindowsOs() {
        when(System.getProperty(eq("os.name"))).thenReturn("Linux Debian");
        assertEquals(false, EnvironmentUtil.isWindows());
    }

    @Test
    public void detectsWindowsOs() {
        when(System.getProperty(eq("os.name"))).thenReturn("Windows 4.0");
        assertEquals(true, EnvironmentUtil.isWindows());
    }

    @Test
    public void terminalWidthIsGivenCorrectly() {
        when(System.getenv(eq("COLUMNS"))).thenReturn("40");
        assertEquals(40, EnvironmentUtil.getTerminalWidth());
    }

    @Test
    public void getDefaultWidthWhenEnvIsNull() {
        when(System.getenv(eq("COLUMNS"))).thenReturn(null);
        assertEquals(70, EnvironmentUtil.getTerminalWidth());
    }

    @Test
    public void getDefaultWidthWhenEnvIsEmpty() {
        when(System.getenv(eq("COLUMNS"))).thenReturn("");
        assertEquals(70, EnvironmentUtil.getTerminalWidth());
    }

    @Test
    public void constructorInitializesFields() throws Exception {
        Properties mockProperties = mock(Properties.class);
        PowerMockito.whenNew(Properties.class)
                .withAnyArguments().thenReturn(mockProperties);
        when(mockProperties.get(eq("version"))).thenReturn("0.1.0");
        assertEquals("0.1.0", EnvironmentUtil.getVersion());
    }
}
