package fi.helsinki.cs.tmc.cli.tmcstuff;

import java.io.File;

public class DirectoryUtil {
    private File courseDirectory;
    private File configFile;
    private String exercise;

    public DirectoryUtil() {
        this.courseDirectory = new File(System.getProperty("user.dir"));

        while (this.courseDirectory != null) {
            this.configFile = new File(
                    this.courseDirectory.getPath() + File.pathSeparator + ".tmc.json");
            if (this.configFile.exists()) {
                break;
            }
            this.exercise = this.courseDirectory.getName();
            this.courseDirectory = this.courseDirectory.getParentFile();
        }
    }

    public File getConfigFile() {
        return configFile;
    }

    public String getExerciseDirectory() {
        return exercise;
    }

    public File getCourseDirectory() {
        return courseDirectory;
    }
}
