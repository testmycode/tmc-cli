package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import java.nio.file.Path;

@Command(name = "submit", desc = "Submit exercises")
public class SubmitCommand implements CommandInterface {

    private Application app;
    private Io io;

    public SubmitCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {
        TmcCore core;
        Course course;
        SubmissionResult submit;
        DirectoryUtil dirUtil;

        this.io = io;
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
            submit = core.submit(new TmcCliProgressObserver(),
                    TmcUtil.findExercise(course, exerciseName)).call();

        } catch (Exception e) {
            return;
        }

        printResults(submit);
    }

    private void printResults(SubmissionResult result) {
        if (result.isAllTestsPassed()) {
            io.println(Color.colorString("All tests passed!", Color.ANSI_GREEN));
        } else {
            io.println(Color.colorString("Tests failed.", Color.ANSI_RED));
        }

        System.out.println("\n\n\n\n" + result.toString());
    }
}
