package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ExternalsUtil;
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
        options.addOption("n", "no-pager", false, "Don't use a pager to list the exercises");
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
            WorkDir workDir = getApp().getWorkDir();
            workDir.addPath();

            if (workDir.getConfigFile() != null) {
                CourseInfo courseinfo = CourseInfoIo.load(workDir.getConfigFile());
                courseName = courseinfo.getCourseName();

            } else {
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
        printExercises(core, courseName, !args.hasOption("n"));
    }

    private void printExercises(TmcCore core, String name, Boolean pager) {
        List<Exercise> exercises;
        Course course;

        course = TmcUtil.findCourse(core, name);
        if (course == null) {
            this.io.println("Course '" + name + "' doesn't exist.");
            return;
        }

        exercises = course.getExercises();
        if (exercises.isEmpty()) {
            this.io.println("Course '" + name + "' doesn't have any exercises.");
        }

        StringBuilder sb = new StringBuilder("Course name: " + course.getName());
        String prevDeadline = "";

        for (Exercise exercise : exercises) {
            String deadline = getDeadline(exercise);
            if (!deadline.equals(prevDeadline)) {
                sb.append("\nDeadline: " + deadline + "\n");
            }
            prevDeadline = deadline;
            
            sb.append(getExerciseStatus(exercise));

        }
        if (pager) {
            ExternalsUtil.showStringInPager(sb.toString(), "exercise-list");
        } else {
            io.print(sb.toString());
        }
    }

    private String getDeadline(Exercise exercise) {
        String deadline = exercise.getDeadline();
        if (deadline == null) {
            return "not available";
        }
        deadline = deadline.substring(0, 19);
        return deadline.replace("T", " at ");
    }

    private String getExerciseStatus(Exercise exercise) {
        // Check the exercise status in order of flag importance, for example there's
        // no need to check if deadline has passed if the exercise has been submitted
        String status;
        if (exercise.isCompleted()) {
            if (exercise.requiresReview() && !exercise.isReviewed()) {
                status = Color.colorString("  Requires review: ", Color.AnsiColor.ANSI_YELLOW);
            } else {
                status = Color.colorString("  Completed: ", Color.AnsiColor.ANSI_GREEN);
            }
        } else if (exercise.hasDeadlinePassed()) {
            status = Color.colorString("  Deadline passed: ", Color.AnsiColor.ANSI_PURPLE);
        } else if (exercise.isAttempted()) {
            status = Color.colorString("  Attempted: ", Color.AnsiColor.ANSI_BLUE);
        } else {
            status = Color.colorString("  Not completed: ", Color.AnsiColor.ANSI_RED);
        }
        
        status += exercise.getName() + "\n";
        return status;
    }
}
