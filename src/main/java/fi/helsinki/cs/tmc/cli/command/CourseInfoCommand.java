package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "course-info", desc = "Show course info for a specific course")
public class CourseInfoCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(ListCoursesCommand.class);
    private Application app;

    public CourseInfoCommand(Application app) {
        this.app = app;
    }

    @Override
    public void run(String[] args, Io io) {
        TmcCore core = this.app.getTmcCore();
        if (core == null) {
            return;
        }
        
        if (args.length == 0) {
            io.println("You must give the course name as a parameter.");
            return;
        }
        
        Course course = TmcUtil.findCourse(core, args[0]);
        if (course == null) {
            io.println("The course doesn't exist on this server.");
        }
        printCourse(course, io);
    }
    
    private void printCourse(Course course, Io io) {
        io.println("Course name: " + course.getName());
        io.println("Course id: " + course.getId());
        io.println("Number of exercises: " + course.getExercises().size());
    }
    
}
