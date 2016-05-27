package fi.helsinki.cs.tmc.cli.tmcstuff;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryUtil {
    private Path courseDirectory;
    private Path configFile;
    private String exercise;

    public DirectoryUtil() {
        this.courseDirectory = Paths.get(System.getProperty("user.dir"));

        while (this.courseDirectory.getParent() != null) {
            this.configFile = this.courseDirectory.resolve(CourseInfoIo.COURSE_CONFIG);
            if (Files.exists(this.configFile)) {
                return;
            }
            this.exercise = this.courseDirectory
                    .getName(this.courseDirectory.getNameCount() - 1)
                    .toString();;
            this.courseDirectory = this.courseDirectory.getParent();
        }
        this.exercise = null;
        this.configFile = null;
        this.courseDirectory = null;
    }

    /**
     * If called from a course directory or any of its subdirectories,
     * return the appropriate course config file (.tmc.json)
     */
    public Path getConfigFile() {
        return configFile;
    }

    /**
     * If called from an exercise directory, return the name of the exercise.
     * Otherwise null, even if in a course directory.
     */
    public String getExerciseName() {
        return exercise;
    }

    public Path getCourseDirectory() {
        return courseDirectory;
    }
}
