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
            this.configFile = Paths.get(
                    this.courseDirectory.toString() + File.separator + ".tmc.json");
            if (Files.exists(this.configFile)) {
                break;
            }
            this.exercise = this.courseDirectory.toString();
            this.courseDirectory = this.courseDirectory.getParent();
        }
    }

    public Path getConfigFile() {
        return configFile;
    }

    public String getExerciseName() {
        return exercise;
    }

    public Path getCourseDirectory() {
        return courseDirectory;
    }
}
