package fi.helsinki.cs.tmc.cli.updater;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
    static String malformedJson;
    static String changedJson;

    TestIo io;

    @BeforeClass
    public static void setUpClass() throws IOException {
        latestJson = readResource("test-jsons/latest.json");
        assertNotNull(latestJson);

        apiLimitExeededJson = readResource("test-jsons/api_rate_limit_exeeded.json");
        assertNotNull(apiLimitExeededJson);

        malformedJson = readResource("test-jsons/malformed.json");
        assertNotNull(malformedJson);

        changedJson = readResource("test-jsons/changed.json");
        assertNotNull(changedJson);
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

    @Test
    public void doNothingIfFetchingReleaseJsonFails() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        when(updater.fetchLatestReleaseJson()).thenReturn(null);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    @Test
    public void doNothingIfFetchedJsonIsApiRateLimitExceeded() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        when(updater.fetchLatestReleaseJson()).thenReturn(apiLimitExeededJson);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    @Test
    public void doNothingIfUserDoesntWantToUpdate() {
        io.addLinePrompt("n");
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        updater.run();
        assertThat(io.out(), containsString("A new version of tmc-cli is available!"));
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
    }

    @Test
    public void downloadsAndRunsNewBinaryIfOk() {
        io.addLinePrompt("yes");
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        doNothing().when(updater).fetchTmcCliBinary(any(String.class), any(File.class));
        doNothing().when(updater).runNewTmcCliBinary(any(String.class));
        updater.run();
        assertThat(io.out(), containsString("A new version of tmc-cli is available!"));
        assertThat(io.out(), containsString("Downloading..."));
        verify(updater, times(1)).fetchTmcCliBinary(any(String.class), any(File.class));
        verify(updater, times(1)).runNewTmcCliBinary(any(String.class));
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

    @Test
    public void doNothingIfJsonIsMalformed() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        when(updater.fetchLatestReleaseJson()).thenReturn(malformedJson);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    @Test
    public void abortIfJsonDoesNotContainNecessaryInfo() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        when(updater.fetchLatestReleaseJson()).thenReturn(changedJson);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
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
