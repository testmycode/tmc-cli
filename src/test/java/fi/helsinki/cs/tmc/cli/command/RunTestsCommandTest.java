package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

public class RunTestsCommandTest {
    Application app;
    TestIo io;
    TmcCore mockCore;

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void givesAnErrorMessageIfNotInCourseDirectory() {
        WorkDir workDir = new WorkDir(Paths.get(System.getProperty("java.io.tmpdir")));
        app.setWorkdir(workDir);
        String[] args = {"run-tests"};
        app.run(args);
        assertTrue(io.getPrint().contains("You have to be in the exercise root"));
    }

    @Test
    public void worksInCourseDirectory() {
        
    }

}