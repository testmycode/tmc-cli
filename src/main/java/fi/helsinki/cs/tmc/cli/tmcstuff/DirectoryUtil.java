package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DirectoryUtil {
    private Path courseDirectory;
    private Path configFile;
    private String exercise;

    public DirectoryUtil(Path subdir) {
        this.courseDirectory = Paths.get(System.getProperty("user.dir"));
        if (subdir != null) {
            this.courseDirectory = this.courseDirectory.resolve(subdir);
        }
        this.exercise = null;

        while (this.courseDirectory.getParent() != null) {
            this.configFile = this.courseDirectory.resolve(CourseInfoIo.COURSE_CONFIG);
            if (Files.exists(this.configFile)) {
                return;
            }
            if (this.exercise == null) {
                this.exercise = this.courseDirectory
                        .getName(this.courseDirectory.getNameCount() - 1)
                        .toString();
            } else {
                this.exercise = this.courseDirectory
                        .getName(this.courseDirectory.getNameCount() - 1)
                        .toString() + "-" + this.exercise;
            }
            this.courseDirectory = this.courseDirectory.getParent();
        }
        this.exercise = null;
        this.configFile = null;
        this.courseDirectory = null;
    }

    public DirectoryUtil() {
        this(null);
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
     * ***SOON TO BE DEPRECATED, SEE getExerciseNames() BELOW***
     */
    public String getExerciseName() {
        return exercise;
    }

    /**
     * Parse parametres and return all matching exercises by reading
     * the course config file
     * @param: arguments given when searching for exercises
     * @return: return exercises as List
     */
    public List<String> getExerciseNames(String[] params) {
        Path dir = Paths.get(System.getProperty("user.dir"));
        for (int i = 0; i < params.length; i++) {
            // Convert given parametres to either full exercise names
            // Or substrings, eg. "viikko1/teht1" -> "viikko1-teht1"
            String param = this.courseDirectory.relativize(dir.resolve(params[1])).toString();
            params[i] = param.replace(File.separator, "-");
        }
        CourseInfoIo infoio = new CourseInfoIo(this.configFile);
        CourseInfo info = infoio.load();
        List<Exercise> exercises = info.getExercises();
        List<String> exerciseNames = new ArrayList<String>();
        for (Exercise exercise : exercises) {
            for (String param : params) {
                if (exercise.getName().matches("^" + param)) {
                    exerciseNames.add(exercise.getName());
                }
            }
        }
        return exerciseNames;
    }

    /**
     * Returns the root directory of the course containing the config file
     */
    public Path getCourseDirectory() {
        return courseDirectory;
    }
}
