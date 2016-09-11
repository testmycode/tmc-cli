package fi.helsinki.cs.tmc.cli.command.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.core.TmcCore;

import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

public class AdminCommadTest {

    private static final String EXERCISE_PATH = "--exercise-path";
    private static final String OUTPUT_PATH = "--output-path";
    private static final String LOCALE = "--locale";

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;
    private TaskExecutor mockLangs;

    private static Path exercisePath;

    @Before
    public void setUp() throws URISyntaxException {
        exercisePath =
                Paths.get(
                        AdminCommadTest.class
                                .getClassLoader()
                                .getResource("dummy-courses/admin_arith_funcs")
                                .toURI());
        io = new TestIo();
        mockCore = mock(TmcCore.class);
        mockLangs = mock(TaskExecutor.class);
        ctx = new CliContext(io, mockCore, null, mockLangs);
        app = new Application(ctx);
    }

//    @Test
//    public void printsAnErrorMessageIfGivenCourseName() {
//        String[] args = {"admin", "course"};
//        app.run(args);
//        io.assertContains("Use in the course directory");
//    }

    @Test
    public void testScanExercise() throws NoLanguagePluginFoundException {
        final String outputPath = exercisePath + "/checkstyle.txt";

        when(mockLangs.scanExercise(exercisePath, "admin_arith_funcs"))
                .thenReturn(
                        Optional.of(
                                new ExerciseDesc(
                                        "Name", ImmutableList.copyOf(new ArrayList<TestDesc>()))));

        String[] args = {
            "admin", "scan-exercise", "--exercise", exercisePath.toString(), "--output", outputPath
        };
        app.run(args);

        verify(mockLangs).scanExercise(exercisePath, "admin_arith_funcs");
        io.assertContains(
                "Exercises scanned successfully, results can be found in "
                        + outputPath
                        + "\n");
//        assertTrue(
//                "Error output should be clean, but it was " + mio.getSysErr(),
//                mio.getSysErr().isEmpty());
    }

    @Test
    public void testRunTests() throws NoLanguagePluginFoundException {
        final String outputPath = exercisePath + "/results.txt";

        String[] args = {
            "admin", "run-tests", "--exercise", exercisePath.toString(), "--output", outputPath
        };
        app.run(args);

        verify(mockLangs).runTests(exercisePath);
        io.assertContains("Test results can be found in " + outputPath + "\n");
//        assertTrue(
//                "Error output should be clean, but it was " + mio.getSysErr(),
//                mio.getSysErr().isEmpty());
    }

    @Test
    public void testRunCheckCodeStyle() throws NoLanguagePluginFoundException {
        final String outputPath = exercisePath + "/exercises.txt";

        String[] args = {
            "admin", "checkstyle",
            "--exercise", exercisePath.toString(),
            "--output", outputPath,
            "--locale", "en"
        };
        app.run(args);

        verify(mockLangs).runCheckCodeStyle(exercisePath, new Locale("en"));
        io.assertContains("Codestyle report can be found at " + outputPath + "\n");
//        assertTrue(
//                "Error output should be clean, but it was " + mio.getSysErr(),
//                mio.getSysErr().isEmpty());
    }

    @Test
    public void testPrepareStub() {

        String[] args = {"prepare-stub", "--exercise", exercisePath.toString()};
        app.run(args);

        // Mockito.verify(executor).prepareStub(stubPath);
    }

    @Test
    public void testPrepareSolution() {

        String[] args = {"admin", "prepareSolution", "--exercise", exercisePath.toString()};
        app.run(args);

        // Why is this commented out?
        // Mockito.verify(executor).prepareSolution(solutionPath);
    }
}
