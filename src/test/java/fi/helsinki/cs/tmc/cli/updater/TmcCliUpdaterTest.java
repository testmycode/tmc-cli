package fi.helsinki.cs.tmc.cli.updater;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.IOException;

public class TmcCliUpdaterTest {

    static String latestJson;
    static String apiLimitExeededJson;

    @BeforeClass
    public static void setUpClass() throws IOException {
        latestJson = readResource("latest.json");
        assertNotNull(latestJson);

        apiLimitExeededJson = readResource("api_rate_limit_exeeded.json");
        assertNotNull(apiLimitExeededJson);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void stub() {
        assertTrue(true);
    }

    private static String readResource(String resourceName) throws IOException {
        InputStream inputStream = TmcCliUpdaterTest.class.getClassLoader()
                .getResourceAsStream(resourceName);
        if (inputStream == null) {
            return null;
        }
        String resourceContent = IOUtils.toString(inputStream, "UTF-8");
        IOUtils.closeQuietly(inputStream);
        return resourceContent;
    }
}
