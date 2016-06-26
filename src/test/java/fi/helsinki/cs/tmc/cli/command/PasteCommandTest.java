package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ExternalsUtil.class, CourseInfoIo.class, TmcUtil.class})
public class PasteCommandTest {

    private Application app;
    private TestIo io;
    private TmcCore mockCore;
    private ArrayList<String> exerciseNames;
    private Exercise exercise;
    private WorkDir workDir;

    private final URI pasteUri;

    public PasteCommandTest() throws URISyntaxException {
        pasteUri = new URI("www.abc.url");
    }

    @Before
    public void setup() throws URISyntaxException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("paste-test");
        io = new TestIo();

        workDir = mock(WorkDir.class);
        when(workDir.getCourseDirectory()).thenReturn(tempDir);
        when(workDir.getConfigFile()).thenReturn((tempDir.resolve(CourseInfoIo.COURSE_CONFIG)));
        exerciseNames = new ArrayList<>();
        exerciseNames.add("paste-exercise");
        when(workDir.getExerciseNames()).thenReturn(exerciseNames);
        when(workDir.addPath()).thenReturn(true);
        when(workDir.addPath(anyString())).thenReturn(true);

        mockCore = mock(TmcCore.class);

        app = new Application(io, workDir);
        app.setTmcCore(mockCore);

        CourseInfo mockCourseInfo = mock(CourseInfo.class);
        exercise = new Exercise("paste-exercise");
        when(mockCourseInfo.getExercise("paste-exercise")).thenReturn(exercise);

        mockStatic(TmcUtil.class);
        mockStatic(ExternalsUtil.class);
        when(ExternalsUtil
                .getUserEditedMessage(anyString(), anyString(), anyBoolean()))
                .thenReturn("This is my paste message!");

        mockStatic(CourseInfoIo.class);
        when(CourseInfoIo
                .load(any(Path.class)))
                .thenReturn(mockCourseInfo);
        when(CourseInfoIo
                .save(any(CourseInfo.class), any(Path.class)))
                .thenReturn(true);
    }

    @Test
    public void failIfCoreIsNull() {
        Application application = spy(new Application(io, workDir));
        doReturn(null).when(application).getTmcCore();

        String[] args = {"paste"};
        application.run(args);
        io.assertNotContains("No exercise specified");
    }

    @Test
    public void pasteRunsRightWithoutArguments() throws URISyntaxException {
        when(TmcUtil.sendPaste(eq(mockCore), any(Exercise.class), anyString()))
                .thenReturn(pasteUri);
        io.addConfirmationPrompt(true);
        app.run(new String[] {"paste", "paste-exercise"});

        verifyStatic(times(1));
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(times(1));
        TmcUtil.sendPaste(eq(mockCore), any(Exercise.class), anyString());

        io.assertContains("Paste sent for exercise paste-exercise");
        assertTrue("Prints the paste URI",
                io.out().contains(pasteUri.toString()));
        io.assertAllPromptsUsed();
    }

    @Test
    public void pasteRunsRightWithMessageSwitchWithMessage() {
        when(TmcUtil.sendPaste(eq(mockCore), any(Exercise.class), anyString()))
                .thenReturn(pasteUri);
        app.run(new String[] {"paste", "-m", "This is a message given as an argument",
                "paste-exercise"});

        verifyStatic(Mockito.never());
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(Mockito.times(1));
        TmcUtil.sendPaste(eq(mockCore), eq(exercise),
                eq("This is a message given as an argument"));

        io.assertContains("Paste sent for exercise paste-exercise");
        assertTrue("Prints the paste URI",
                io.out().contains(pasteUri.toString()));
    }

    @Test
    public void pasteFailsWithMessageSwitchWithoutMessage() {
        app.run(new String[]{"paste", "-m"});
        verifyStatic(Mockito.never());
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        assertTrue("Prints to IO when failing to parse",
                io.out().contains("Invalid"));
    }

    @Test
    public void pasteRunsRightWithNoMessageSwitch() {
        when(TmcUtil.sendPaste(eq(mockCore), any(Exercise.class), anyString()))
                .thenReturn(pasteUri);
        app.run(new String[] {"paste", "-n", "paste-exercise"});

        verifyStatic(Mockito.never());
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(Mockito.times(1));
        TmcUtil.sendPaste(eq(mockCore), eq(exercise),
                eq(""));

        io.assertContains("Paste sent for exercise paste-exercise");
        assertTrue("Prints the paste URI",
                io.out().contains(pasteUri.toString()));
    }

    @Test
    public void handlesExceptionWhenCallableFails() {
        io.addConfirmationPrompt(true);
        when(TmcUtil.sendPaste(eq(mockCore), any(Exercise.class), anyString()))
                .thenReturn(null);
        app.run(new String[] {"paste", "paste-exercise"});

        verifyStatic(Mockito.times(1));
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(Mockito.times(1));
        TmcUtil.sendPaste(eq(mockCore), eq(exercise), anyString());

        io.assertContains("Unable to send the paste");
        io.assertAllPromptsUsed();
    }

    @Test
    public void failsWithNoExercise() {
        Mockito.when(workDir.getExerciseNames()).thenReturn(new ArrayList<String>());
        Mockito.when(workDir.addPath()).thenReturn(false);
        Mockito.when(workDir.addPath(anyString())).thenReturn(false);
        app.run(new String[] {"paste", "-m", "This is a message given as an argument"});

        verifyStatic(Mockito.never());
        TmcUtil.sendPaste(eq(mockCore), any(Exercise.class), anyString());

        io.assertContains("No exercise specified");
    }
}
