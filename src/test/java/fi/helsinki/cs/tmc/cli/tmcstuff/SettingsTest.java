package fi.helsinki.cs.tmc.cli.tmcstuff;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SettingsTest {

    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings(null, null, null);
    }

    @Test
    public void correctApiVersion() {
        assertEquals("7", settings.apiVersion());
    }
}
