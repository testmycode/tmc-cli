package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SubmitCommand implements Command {
    private Application app;

    public SubmitCommand(Application app) {
        this.app = app;
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
        DirectoryUtil dirUtil;

        dirUtil = new DirectoryUtil();
        Path dir = dirUtil.getCourseDirectory();

        if (dir == null) {
            System.out.println("Not a course directory");
            return;
        }

        core = this.app.getTmcCore();
        String courseName = dir.getName(dir.getNameCount() - 1).toString();
        course = TmcUtil.findCourse(core, courseName);
        String exerciseName;

        Path directory = Paths.get(System.getProperty("user.dir"));

        // If no args given, use the exercise directory where the program is run
        if (args.length > 0) {
            if (!directory.equals(dirUtil.getCourseDirectory())) {
                exerciseName = directory.getName(directory.getNameCount() - 1)
                        .toString() + "-" + args[0];
            } else {
                exerciseName = args[0];
            }
        } else {
            exerciseName = dirUtil.getExerciseName();
        }
        System.out.println(exerciseName);

        SubmissionResult result;
        result = TmcUtil.submitExercise(core, course, exerciseName);

        System.out.println(result);
    }
}
