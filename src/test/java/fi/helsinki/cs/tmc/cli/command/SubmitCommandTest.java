package fi.helsinki.cs.tmc.cli.command;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.TmcCore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SubmitCommandTest {

    static Path pathToDummyCourse;
    static Path pathToDummyExercise;

    Application app;
    TestIo io;
    TmcCore mockCore;

    @BeforeClass
    public static void setUpClass() {
        pathToDummyCourse = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/2016-aalto-c")
                .getPath());

        pathToDummyExercise = Paths.get(SubmitCommandTest.class.getClassLoader()
                .getResource("dummy-courses/2016-aalto-c/Module_1-02_intro")
                .getPath());

        assertNotNull(pathToDummyCourse);
        assertNotNull(pathToDummyExercise);
    }

    @Before
    public void setUp() {
        io = new TestIo();
        app = new Application(io);
        mockCore = mock(TmcCore.class);
        app.setTmcCore(mockCore);
    }

    @Test
    public void stub() {
        assertTrue(true);
    }
}
