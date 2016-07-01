package fi.helsinki.cs.tmc.cli.io;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Runtime.class, EnvironmentUtil.class})
public class ExternalUtilTest {
    
    private static String filename;

    @Before
    public void setUp() {
        filename = System.getProperty("java.io.tmpdir");
        mockStatic(Runtime.class);
        mockStatic(EnvironmentUtil.class);
        when(EnvironmentUtil.isWindows()).thenReturn(false);
    }

    @Ignore
    @Test
    public void getUserEditedMessage() {
        //when(EnvironmentUtil.runProcess(, true)).thenReturn(true);
        ExternalsUtil.getUserEditedMessage("template", filename, false);
    }

    @Ignore
    @Test
    public void showStringInPager() {
        ExternalsUtil.showStringInPager("Show", filename);
    }

    @Ignore
    @Test
    public void showFileInPager() {
        ExternalsUtil.showFileInPager(Paths.get(filename));
    }

    @Ignore
    @Test
    public void runUpdater() {
        Io io = new TestIo();
        String pathToNewBinary = "/tmp/test";
        ExternalsUtil.runUpdater(io, pathToNewBinary);
    }

    @Ignore
    @Test
    public void openInBrowser() throws URISyntaxException {
        URI uri = new URI("http://example.com");
        ExternalsUtil.openInBrowser(uri);
    }
}
