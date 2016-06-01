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
    private Path workingDirectory;
    private String exercise;

    public DirectoryUtil(Path workingDir, Path subDir) {
        if (subDir != null) {
            this.workingDirectory = workingDir.resolve(subDir);
        } else {
            this.workingDirectory = workingDir;
        }
        this.courseDirectory = this.workingDirectory;
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

    public DirectoryUtil(Path subDir) {
        this(Paths.get(System.getProperty("user.dir")), subDir);
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
     * Parse parametres and return all matching exercises by reading the course config file.
     * @param: arguments given when searching for exercises
     * @return: return exercises as List
     */
    public List<String> getExerciseNames(String[] params) {
        CourseInfoIo infoio = new CourseInfoIo(this.configFile);
        CourseInfo info = infoio.load();
        List<Exercise> exercises = info.getExercises();
        List<String> exerciseNames = new ArrayList<>();
        if (params == null || params.length == 0) {
            if (this.workingDirectory.equals(this.courseDirectory)) {
                // In course root dir and no params - return all exercises
                for (Exercise exercise : exercises) {
                    exerciseNames.add(exercise.getName());
                }
                return exerciseNames;
            }
            // If parametres are empty but we are in a subdirectory, create an array
            // with a single element and make that element the relative subdirectory
            params = new String[1];
            String param = this.courseDirectory
                    .relativize(this.workingDirectory).toString();
            params[0] = param.replace(File.separator, "-");
        } else {
            for (int i = 0; i < params.length; i++) {
                // Convert given parametres to either full exercise names
                // Or substrings, eg. if the user is in subdirectory called "viikko1"
                // and gives parametre "teht2", it will be converted to "viikko1-teht2"
                String param = this.courseDirectory
                        .relativize(this.workingDirectory.resolve(params[i])).toString();
                params[i] = param.replace(File.separator, "-");
            }
        }
        for (Exercise exercise : exercises) {
            for (String param : params) {
                // Match only exercises that begin with our parametres, so that
                // exercises with identical names in other subdirectories won't
                // be selected.
                if (exercise.getName().matches("^" + param + ".*")) {
                    exerciseNames.add(exercise.getName());
                }
            }
        }
        return exerciseNames;
    }

    /**
     * Returns the root directory of the course containing the config file.
     */
    public Path getCourseDirectory() {
        return courseDirectory;
    }

    public Path getWorkingDirectory() {
        return this.workingDirectory;
    }
}
