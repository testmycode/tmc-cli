package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ListExercisesCommandTest {
    
    Application app;
    Io mockIo;

    @Before
    public void setUp() {
        mockIo = mock(TerminalIo.class);
        app = new Application(mockIo);
        app.createTmcCore(new Settings(true));
    }

    @Test
    public void runWorksRight() {
        String[] args = {"list-exercises", "demo"};
        app.run(args);
        verify(mockIo).println(Mockito.contains("HeiMaailma"));
    }

    @Test
    public void emptyArgsGivesAnErrorMessage() {
        String[] args = {"list-exercises"};
        app.run(args);
        verify(mockIo).println(Mockito.contains("No course specified"));
    }

}
