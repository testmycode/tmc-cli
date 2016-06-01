package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import java.nio.file.Path;
import java.util.List;

@Command(name = "submit", desc = "Submit exercises")
public class SubmitCommand implements CommandInterface {
    private Application app;

    public SubmitCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {
        TmcCore core;
        Course course;
        DirectoryUtil dirUtil;

        dirUtil = new DirectoryUtil();
        Path courseDir = dirUtil.getCourseDirectory();

        if (courseDir == null) {
            System.out.println("Not a course directory");
            return;
        }
        CourseInfoIo infoIo = new CourseInfoIo(dirUtil.getConfigFile());
        CourseInfo info = infoIo.load();
        String courseName = info.getCourse();
        core = this.app.getTmcCore();
        course = TmcUtil.findCourse(core, courseName);

        List<String> exercises;
        exercises = dirUtil.getExerciseNames(args);
        SubmissionResult result;

        for (String exerciseName : exercises) {
            System.out.println("Submitting: " + exerciseName);
            result = TmcUtil.submitExercise(core, course, exerciseName);
            System.out.println(result);
        }
    }
}

