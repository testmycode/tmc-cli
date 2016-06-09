package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ExternalsUtil.class,CourseInfoIo.class})
public class PasteCommandTest {

    Application app;
    TestIo testIo;
    TmcCore mockCore;
    ArrayList<String> exercise;

    @Before
    public void setup() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        testIo = new TestIo();

        WorkDir workDir = Mockito.spy(new WorkDir(tempDir, null));
        when(workDir.getCourseDirectory())
                .thenReturn(tempDir.resolve("paste-test"));

        exercise = new ArrayList<>();
        exercise.add("paste-exercise");

        Mockito.when(workDir.getExerciseNames(any(String[].class))).thenReturn(exercise);

        Callable mockCallable = mock(Callable.class);
        Callable mockCallableFail = mock(Callable.class);
        try {
            when(mockCallable.call()).thenReturn(URI.create("https://tmc.test.url/"));
            when(mockCallableFail.call()).thenThrow(new Exception());
        } catch (Exception e) {
            // ?????
            // Ignore this stupid block, IntelliJ complains about exceptions if this doesn't exist
        }

        mockCore = mock(TmcCore.class);
        when(mockCore.pasteWithComment(
                any(ProgressObserver.class), any(Exercise.class), anyString()))
                .thenReturn(mockCallable);

        app = new Application(testIo, workDir);

        app = Mockito.spy(app);
        app.setTmcCore(mockCore);

        CourseInfo mockCourseInfo = mock(CourseInfo.class);
        when(mockCourseInfo.getExercise(anyString())).thenReturn(new Exercise("paste-exercise"));

        PowerMockito.mockStatic(ExternalsUtil.class);
        when(ExternalsUtil
                .getUserEditedMessage(anyString(), anyString(), anyBoolean()))
                .thenReturn("This is my paste message!");

        PowerMockito.mockStatic(CourseInfoIo.class);
        when(CourseInfoIo
                .load(any(Path.class)))
                .thenReturn(mockCourseInfo);
        when(CourseInfoIo
                .save(any(CourseInfo.class), any(Path.class)))
                .thenReturn(true);
    }

    @Test
    public void pasteRunsRightWithoutArguments() {
        app.run(new String[] {"paste"});
        assertTrue("Prints to IO when successful",
                testIo.out().contains("Paste sent for exercise paste-exercise"));
        assertTrue("Prints the paste URI",
                testIo.out().contains("https://tmc.test.url/"));
    }
}
