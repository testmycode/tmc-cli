package fi.helsinki.cs.tmc.cli.command;

/**
 * Class is a test command class
 */
public class ListCoursesCommand implements Command {
    @Override
    public String getDescription() {
        return "List the available courses.";
    }

    @Override
    public String getName() {
        return "list-courses";
    }

    @Override
    public void run() {
        System.out.println("test.");
    }
}
