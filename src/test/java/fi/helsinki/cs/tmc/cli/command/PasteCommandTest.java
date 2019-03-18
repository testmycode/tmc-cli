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
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.*;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.snapshots.EventSendBuffer;
import fi.helsinki.cs.tmc.snapshots.EventStore;
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
@PrepareForTest({ExternalsUtil.class, CourseInfoIo.class, TmcUtil.class, SettingsIo.class})
public class PasteCommandTest {

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore core;
    private WorkDir workDir;
    private ArrayList<Exercise> exercises;
    private Exercise exercise;
    private AnalyticsFacade analyticsFacade;
    private AccountList list;

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
        exercise = new Exercise("paste-exercise");
        exercises = new ArrayList<>();
        exercises.add(exercise);
        when(workDir.getExercises()).thenReturn(exercises);
        when(workDir.addPath(anyString())).thenReturn(true);

        Settings settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        core = new TmcCore(settings, tmcLangs);
        EventSendBuffer eventSendBuffer = new EventSendBuffer(new EventStore());
        AnalyticsFacade analyticsFacade = new AnalyticsFacade(eventSendBuffer);

        ctx = new CliContext(io, core, workDir, new Settings(), analyticsFacade);
        app = new Application(ctx);

        CourseInfo mockCourseInfo = mock(CourseInfo.class);
        when(mockCourseInfo.getExercise("paste-exercise")).thenReturn(exercise);

        mockStatic(TmcUtil.class);
        mockStatic(ExternalsUtil.class);
        when(ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean()))
                .thenReturn("This is my paste message!");
        list = new AccountList();
        list.addAccount(new Account("username"));

        mockStatic(SettingsIo.class);
        when(SettingsIo.loadAccountList()).thenReturn(list);

        mockStatic(CourseInfoIo.class);
        when(CourseInfoIo.load(any(Path.class))).thenReturn(mockCourseInfo);
        when(CourseInfoIo.save(any(CourseInfo.class), any(Path.class))).thenReturn(true);
    }

    @Test
    public void doNotRunIfNotLoggedIn() {
        when(CourseInfoIo.load(any(Path.class))).thenReturn(null);
        when(SettingsIo.loadAccountList()).thenReturn(new AccountList());
        app = new Application(ctx);

        String[] args = {"paste"};
        app.run(args);
        io.assertContains("You are not logged in");
    }

    @Test
    public void setMessageAndNoMessageOption() {
        app = new Application(ctx);

        String[] args = {"paste", "-m", "Message", "-n"};
        app.run(args);
        io.assertContains("You can't have the no-message flag and message set");
    }

    @Test
    public void pasteRunsRightWithoutArguments() throws URISyntaxException {
        when(TmcUtil.sendPaste(eq(ctx), any(Exercise.class), anyString())).thenReturn(pasteUri);
        io.addConfirmationPrompt(true);
        app.run(new String[] {"paste", "paste-exercise"});

        io.assertContains("Paste sent for exercise paste-exercise");
        assertTrue("Prints the paste URI", io.out().contains(pasteUri.toString()));
        io.assertAllPromptsUsed();

        verifyStatic(times(1));
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(times(1));
        TmcUtil.sendPaste(eq(ctx), any(Exercise.class), anyString());
    }

    @Test
    public void pasteRunsRightwithTooManyArguments() {
        app.run(new String[] {"paste", "paste-exercise", "secondArgument"});
        io.assertContains("Error: Too many arguments.");
    }

    @Test
    public void pasteRunsRightWithMessageSwitchWithMessage() {
        when(TmcUtil.sendPaste(eq(ctx), any(Exercise.class), anyString())).thenReturn(pasteUri);
        app.run(
                new String[] {
                    "paste", "-m", "This is a message given as an argument", "paste-exercise"
                });

        io.assertContains("Paste sent for exercise paste-exercise");
        assertTrue("Prints the paste URI", io.out().contains(pasteUri.toString()));

        verifyStatic(Mockito.never());
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(Mockito.times(1));
        TmcUtil.sendPaste(eq(ctx), eq(exercise), eq("This is a message given as an argument"));
    }

    @Test
    public void pasteFailsWithMessageSwitchWithoutMessage() {
        app.run(new String[] {"paste", "-m"});

        assertTrue("Prints to IO when failing to parse", io.out().contains("Invalid"));

        verifyStatic(Mockito.never());
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());
    }

    @Test
    public void pasteRunsRightWithNoMessageSwitch() {
        when(TmcUtil.sendPaste(eq(ctx), any(Exercise.class), anyString())).thenReturn(pasteUri);
        app.run(new String[] {"paste", "-n", "paste-exercise"});

        io.assertContains("Paste sent for exercise paste-exercise");
        assertTrue("Prints the paste URI", io.out().contains(pasteUri.toString()));

        verifyStatic(Mockito.never());
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(Mockito.times(1));
        TmcUtil.sendPaste(eq(ctx), eq(exercise), eq(""));
    }

    @Test
    public void handlesExceptionWhenCallableFails() {
        io.addConfirmationPrompt(true);
        when(TmcUtil.sendPaste(eq(ctx), any(Exercise.class), anyString())).thenReturn(null);
        app.run(new String[] {"paste", "paste-exercise"});

        io.assertContains("Unable to send the paste");
        io.assertAllPromptsUsed();

        verifyStatic(Mockito.times(1));
        ExternalsUtil.getUserEditedMessage(anyString(), anyString(), anyBoolean());

        verifyStatic(Mockito.times(1));
        TmcUtil.sendPaste(eq(ctx), eq(exercise), anyString());
    }

    @Test
    public void failsWithNoExercise() {
        Mockito.when(workDir.getExercises()).thenReturn(new ArrayList<Exercise>());
        Mockito.when(workDir.addPath(anyString())).thenReturn(false);
        app.run(new String[] {"paste", "-m", "This is a message given as an argument"});

        io.assertContains("You are not in exercise directory.");

        verifyStatic(Mockito.never());
        TmcUtil.sendPaste(eq(ctx), any(Exercise.class), anyString());
    }
}
