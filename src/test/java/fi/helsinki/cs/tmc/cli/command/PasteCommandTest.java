package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.ExternalsUtil;

import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ExternalsUtil.class)
public class PasteCommandTest {

    Application app;
    Io testIo;
    TmcCore mockCore;

    @Before
    public void setup() {
        testIo = new TestIo();
        app = new Application(testIo);
        mockCore = mock(TmcCore.class);
        app = Mockito.spy(app);
        when(app.getTmcCore()).thenReturn(mockCore);
        PowerMockito.mockStatic(ExternalsUtil.class);
        when(ExternalsUtil
                .getUserEditedMessage(anyString(), anyString(), anyBoolean()))
                .thenReturn("This is my paste message!");
    }

    @Test
    public void pasteRunsRightWithoutArguments() {

    }
}
