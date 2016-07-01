package fi.helsinki.cs.tmc.cli.updater;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

public class TmcCliUpdaterTest {

    private static String latestJson;
    private static String apiLimitExeededJson;
    private static String malformedJson;
    private static String changedJson;

    private TestIo io;

    @BeforeClass
    public static void setUpClass() throws IOException {
        latestJson = readResource("test-jsons/latest.json");
        assertNotNull(latestJson);

        apiLimitExeededJson = readResource("test-jsons/api_rate_limit_exeeded.json");
        assertNotNull(apiLimitExeededJson);

        malformedJson = latestJson.substring(latestJson.indexOf('{') + 1);
        assertNotNull(malformedJson);

        changedJson = readResource("test-jsons/changed.json");
        assertNotNull(changedJson);
    }

    @Before
    public void setUp() {
        io = new TestIo();
    }

    @Test
    public void stub() {
        assertTrue(true);
    }

    @Test
    public void doNothingIfReleaseEqualsCurrentVersion() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.1", false));
        doReturn(latestJson).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    @Test
    public void doNothingIfFetchingReleaseJsonFails() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        doReturn(null).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(null);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    @Test
    public void doNothingIfFetchedJsonIsApiRateLimitExceeded() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        doReturn(apiLimitExeededJson).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(apiLimitExeededJson);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    @Test
    public void doNothingIfUserDoesntWantToUpdate() {
        io.addConfirmationPrompt(false);
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        doReturn(latestJson).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        updater.run();
        assertThat(io.out(), containsString("A new version of tmc-cli is available!"));
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        io.assertAllPromptsUsed();
    }

    @Test
    public void downloadsAndRunsNewBinaryIfOk() {
        io.addConfirmationPrompt(true);
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        doReturn(latestJson).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        doNothing().when(updater).fetchTmcCliBinary(any(String.class), any(File.class));
        when(updater.runNewTmcCliBinary(any(String.class))).thenReturn(true);
        updater.run();
        assertThat(io.out(), containsString("A new version of tmc-cli is available!"));
        assertThat(io.out(), containsString("Downloading..."));
        verify(updater, times(1)).fetchTmcCliBinary(any(String.class), any(File.class));
        verify(updater, times(1)).runNewTmcCliBinary(any(String.class));
        io.assertAllPromptsUsed();
    }

    // Expected to fail once autoupdater is properly implemented on Windows.
    @Test
    public void newReleaseShowsDownloadLinkOnWindows() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", true));
        doReturn(latestJson).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(latestJson);
        updater.run();
        assertThat(io.out(), containsString("A new version of tmc-cli is available!"));
        assertThat(io.out(), containsString("Download: https://"));
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
    }

    @Test
    public void doNothingIfJsonIsMalformed() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        doReturn(malformedJson).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(malformedJson);
        updater.run();
        verify(updater, never()).fetchTmcCliBinary(any(String.class), any(File.class));
        assertTrue(io.out().isEmpty());
    }

    @Test
    public void abortIfJsonDoesNotContainNecessaryInfo() {
        TmcCliUpdater updater = spy(new TmcCliUpdater(io, "0.1.0", false));
        doReturn(changedJson).when(updater).fetchLatestReleaseJson();
        //when(updater.fetchLatestReleaseJson()).thenReturn(changedJson);
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
