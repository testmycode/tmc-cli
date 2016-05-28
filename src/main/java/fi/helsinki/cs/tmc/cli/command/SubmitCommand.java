package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import java.nio.file.Path;

public class SubmitCommand implements Command {

    private final Io io;
    private Application app;

    public SubmitCommand(Application app) {
        this.app = app;
        this.io = new TerminalIo(); // should be injected?
    }

    @Override
    public String getDescription() {
        return "Submit exercises";
    }

    @Override
    public String getName() {
        return "submit";
    }

    @Override
    public void run(String[] args) {
        TmcCore core;
        Course course;
        SubmissionResult result;
        DirectoryUtil dirUtil;

        dirUtil = new DirectoryUtil();
        core = this.app.getTmcCore();
        Path dir = dirUtil.getCourseDirectory();

        if (dir == null) {
            io.println("You are not in course directory");
            return;
        }
        course = TmcUtil.findCourse(core, dir.getName(dir.getNameCount() - 1).toString());
        String exerciseName = dirUtil.getExerciseName();

        try {
            result = core.submit(ProgressObserver.NULL_OBSERVER,
                    TmcUtil.findExercise(course, exerciseName)).call();

        } catch (Exception e) {
            return;
        }

        System.out.println(result);
    }
}
