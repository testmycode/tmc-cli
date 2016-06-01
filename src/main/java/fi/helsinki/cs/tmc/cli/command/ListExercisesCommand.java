package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.command.core.CommandInterface;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.DirectoryUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.util.List;

@Command(name = "list-exercises", desc = "List the exercises for a specific course")
public class ListExercisesCommand implements CommandInterface {
    private static final Logger logger = LoggerFactory.getLogger(ListExercisesCommand.class);
    private Application app;
    private Io io;

    public ListExercisesCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {
        TmcCore core;
        this.io = io;
        String courseName;

        // If no args given, check if the current directory is a course directory and set courseName as the name of that course.
        // Else, print out a help message.
        if (args.length == 0) {
            DirectoryUtil dirUtil = new DirectoryUtil();
            System.out.println(dirUtil.getConfigFile());

            if (dirUtil.getConfigFile() != null) {
                CourseInfo courseinfo = new CourseInfoIo(dirUtil.getConfigFile()).load();
                courseName = courseinfo.getCourse();

            } else  {
                this.io.println("USAGE: tmc exercise COURSE");
                this.io.println("No course specified. Either run the command "
                        + "inside a course directory or enter\n"
                        + "the course as a parameter.");
                return;
            }

        } else {
            courseName = args[0];
        }

        core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        printExercises(core, courseName);
    }

    // This one can be moved to TmcUtil maybe?
    private void printExercises(TmcCore core, String name) {
        List<Exercise> exercises;
        Course course;

        course = TmcUtil.findCourse(core, name);
        if (course == null) {
            this.io.println("Course doesn't exist.");
            return;
        }

        exercises = course.getExercises();
        for (Exercise exercise : exercises) {
            this.io.println(exercise.getName());
        }
    }
}
