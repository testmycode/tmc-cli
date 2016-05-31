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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (args.length == 0) {
            this.io.println("USAGE: tmc exercise COURSE");
            return;
        }

        core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        printExercises(core, args[0]);
    }

    public void printExercises(TmcCore core, String name) {
        List<Exercise> exercises;
        Course course;
        if (name == null) {
            // If no arguments are given, check if the current directory is a course directory
            DirectoryUtil dirutil = new DirectoryUtil();
            CourseInfo courseinfo = new CourseInfoIo(dirutil.getConfigFile()).load();
        }
        if (name == null) {
            this.io.println("No course specified. Either run the command "
                    + "inside a course directory or enter\n"
                    + "the course as a parametre.");
            return;
        }
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
