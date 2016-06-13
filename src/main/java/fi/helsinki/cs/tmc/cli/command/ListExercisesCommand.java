package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.List;

@Command(name = "exercises", desc = "List the exercises for a specific course")
public class ListExercisesCommand extends AbstractCommand {
    private Io io;

    @Override
    public void getOptions(Options options) {
    }

    @Override
    public void run(CommandLine args, Io io) {
        TmcCore core;
        this.io = io;
        String courseName;

        String[] stringArgs = args.getArgs();

        // If no args given, check if the current directory is a course directory and set courseName as the name of that course.
        // Else, print out a help message.
        if (stringArgs.length == 0) {
            WorkDir dirUtil = new WorkDir();

            if (dirUtil.getConfigFile() != null) {
                CourseInfo courseinfo = CourseInfoIo.load(dirUtil.getConfigFile());
                courseName = courseinfo.getCourseName();

            } else  {
                //TODO replace this with help message.
                this.io.println("USAGE: tmc list-exercises COURSE");
                this.io.println("No course specified. Either run the command "
                        + "inside a course directory or enter\n"
                        + "the course as a parameter.");
                return;
            }

        } else {
            courseName = stringArgs[0];
        }

        core = getApp().getTmcCore();
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
        if (exercises.isEmpty()) {
            this.io.println("Course doesn't have any exercises.");
        }

        for (Exercise exercise : exercises) {
            this.io.println(exercise.getName());
        }
    }
}
