package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;

import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class ListExercisesCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ListExercisesCommand.class);
    private Application app;

    public ListExercisesCommand(Application app) {
        this.app = app;
    }

    @Override
    public String getDescription() {
        return "List the exercises for a specific course";
    }

    @Override
    public String getName() {
        return "list-exercises";
    }

    @Override
    public void run(String[] args) {
        List<Exercise> exercises;
        Course course;
        TmcCore core;

        if (args.length == 0) {
            return;
        }

        core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        course = TmcUtil.findCourse(core, args[0]);
        exercises = course.getExercises();

        for (Exercise exercise : exercises) {
            System.out.println(exercise.getName());
        }
    }
}
