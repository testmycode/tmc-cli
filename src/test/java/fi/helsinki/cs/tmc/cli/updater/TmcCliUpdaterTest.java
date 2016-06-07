package fi.helsinki.cs.tmc.cli.updater;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

public class TmcCliUpdaterTest {

    static String latestJson;
    static String apiLimitExeededJson;

    TestIo io;

    @BeforeClass
    public static void setUpClass() throws IOException {
        latestJson = readResource("latest.json");
        assertNotNull(latestJson);

        apiLimitExeededJson = readResource("api_rate_limit_exeeded.json");
        assertNotNull(apiLimitExeededJson);
    }

    @Before
    public void setUp() {
        io = new TestIo();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void stub() {
        assertTrue(true);
    }

    @Test
    public void doNothingIfReleaseEqualsCurrentVersion() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.1", false));
        when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    // Expected to fail once autoupdater is properly implemented on Windows.
    @Test
    public void newReleaseShowsDownloadLinkOnWindows() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", true));
        when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        updater.run();
        assertThat(io.out(), containsString("A new version of tmc-cli is available!"));
        assertThat(io.out(), containsString("Download: https://"));
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
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
