package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SettingsIo.class)
public class LogoutCommandTest {

    TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();

        PowerMockito.mockStatic(SettingsIo.class);
    }

    @Test
    public void hello() {
        LogoutCommand logoutCommand = new LogoutCommand();
        logoutCommand.run(null, io);

        verifyStatic(times(1));
        SettingsIo.delete();

        assertThat(io.out(), containsString("Logged out."));
    }
}
