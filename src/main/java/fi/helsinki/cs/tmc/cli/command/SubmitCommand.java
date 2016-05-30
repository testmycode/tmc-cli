package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;

import java.io.File;
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

        Path currentDir = Paths.get(System.getProperty("user.dir"));

        // If not in the course directory root, adds the name of the directory you are in to the exerciseName. For courses that are formatted like this: course/week/exercise
        // If no args given, use the exercise directory where the program is run
        if (args.length > 0) {
            if (!currentDir.equals(dirUtil.getCourseDirectory())) {
                exerciseName = currentDir.getName(currentDir.getNameCount() - 1)
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
