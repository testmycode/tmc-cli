package fi.helsinki.cs.tmc.cli.io;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Runtime.class, EnvironmentUtil.class, Desktop.class, ExternalsUtil.class})
public class ExternalUtilTest {

    //TODO add more test cases
    private Path tempDir;

    @Before
    public void setUp() {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("tmc-cli");

        mockStatic(System.class);
        mockStatic(Runtime.class);
        mockStatic(Desktop.class);
        mockStatic(EnvironmentUtil.class);
        when(System.getenv(anyString())).thenReturn(null);
        when(System.getProperty(anyString())).thenCallRealMethod();
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (IOException e) {
            // NOP
        }
    }

    @Test
    public void getUserEditedMessage() {
        when(EnvironmentUtil.isWindows()).thenReturn(false);
        when(EnvironmentUtil.runProcess(any(String[].class), eq(true))).thenReturn(true);

        ExternalsUtil.getUserEditedMessage("template", "test-filename", false);

        ArgumentCaptor<String[]> argsCaptor = ArgumentCaptor.forClass(String[].class);
        verifyStatic(times(1));
        EnvironmentUtil.runProcess(argsCaptor.capture(), eq(true));

        String[] args = argsCaptor.getValue();
        assertEquals(2, args.length);
        assertEquals("nano", args[0]);
        assertThat(args[1], containsString("test-filename-"));
    }

    @Test
    public void showStringInPager() {
        when(EnvironmentUtil.isWindows()).thenReturn(false);
        when(EnvironmentUtil.runProcess(any(String[].class), eq(true))).thenReturn(true);
        ExternalsUtil.showStringInPager("Show", "test-filename-");

        ArgumentCaptor<String[]> argsCaptor = ArgumentCaptor.forClass(String[].class);
        verifyStatic(times(1));
        EnvironmentUtil.runProcess(argsCaptor.capture(), eq(true));

        String[] args = argsCaptor.getValue();
        assertEquals(2, args.length);
        assertEquals("less -R", args[0]);
        assertThat(args[1], containsString("test-filename-"));
    }

    @Test
    public void showFileInPager() {
        Path filename = tempDir.resolve("pager-file");
        when(EnvironmentUtil.isWindows()).thenReturn(false);
        when(EnvironmentUtil.runProcess(any(String[].class), eq(true))).thenReturn(true);
        ExternalsUtil.showFileInPager(filename);

        ArgumentCaptor<String[]> argsCaptor = ArgumentCaptor.forClass(String[].class);
        verifyStatic(times(1));
        EnvironmentUtil.runProcess(argsCaptor.capture(), eq(true));

        String[] args = argsCaptor.getValue();
        assertEquals(2, args.length);
        assertEquals("less -R", args[0]);
        assertEquals(filename.toString(), args[1]);
    }

    @Test
    public void runUpdater() throws IOException {
        Path filename = tempDir.resolve("updater-file");
        File file = filename.toFile();
        file.getParentFile().mkdirs();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write("...");
        }

        when(EnvironmentUtil.isWindows()).thenReturn(false);
        when(EnvironmentUtil.runProcess(any(String[].class), eq(true))).thenReturn(true);
        Io io = new TestIo();
        ExternalsUtil.runUpdater(io, filename.toString());

        ArgumentCaptor<String[]> argsCaptor = ArgumentCaptor.forClass(String[].class);
        verifyStatic(times(2));
        EnvironmentUtil.runProcess(argsCaptor.capture(), eq(true));

        List<String[]> runs = argsCaptor.getAllValues();
        String[] args = runs.get(0);
        assertEquals(3, args.length);
        assertEquals("chmod", args[0]);
        assertEquals("u+x", args[1]);
        assertEquals(filename.toString(), args[2]);

        String[] args2 = runs.get(1);
        assertEquals(2, args2.length);
        assertEquals(filename.toString(), args2[0]);
        assertEquals("++internal-update", args2[1]);
    }

    @Test
    public void openInBrowser() throws URISyntaxException, IOException {
        URI uri = new URI("http://example.com");
        Desktop mockDescktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(mockDescktop);
        ExternalsUtil.openInBrowser(uri);

        verify(mockDescktop, times(1)).browse(eq(uri));
    }

    @Test
    public void notSupportedDesktopInOpenInBrowser() throws URISyntaxException {
        URI uri = new URI("http://example.com");
        when(Desktop.isDesktopSupported()).thenReturn(false);
        ExternalsUtil.openInBrowser(uri);

        verifyStatic(never());
        Desktop.getDesktop();
    }
}
