package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WorkDir {
    // Course root directory. null if n/a.
    private Path courseDirectory;
    // Course config file. null if n/a.
    private Path configFile;
    // Store all paths as absolute paths.
    private List<Path> directories;
    // ONLY OVERRIDE workdir FOR TESTS, IT IS FOR MOCKING THE CURRENT DIRECTORY
    private Path workdir;

    public WorkDir() {
        this.workdir = Paths.get(System.getProperty("user.dir"));
        this.directories = new ArrayList<>();
    }

    public WorkDir(Path path) {
        this();
        this.workdir = path;
    }

    /**
     * Returns the root directory of the course containing the config file.
     */
    public Path getCourseDirectory() {
        if (this.courseDirectory != null) {
            return this.courseDirectory;
        } else {
            return findCourseDir(this.workdir);
        }
    }

    /**
     * If one of the directories is in a course directory,
     * return the appropriate course config file (.tmc.json)
     */
    public Path getConfigFile() {
        if (this.configFile != null) {
            return this.configFile;
        } else {
            Path path = findCourseDir(this.workdir);
            if (path == null) {
                return null;
            }
            return path.resolve(CourseInfoIo.COURSE_CONFIG);
        }
    }

    /**
     * Go through directories and return matching exercises.
     * @return: return names of exercises as List
     */
    public List<String> getExerciseNames() {
        if (this.directories.isEmpty()) {
            if (!addPath()) {
                return new ArrayList<>();
            }
        }
        CourseInfo courseinfo = CourseInfoIo.load(getConfigFile());
        if (courseinfo == null) {
            return new ArrayList<>();
        }
        List<String> allExerciseNames = courseinfo.getExerciseNames();
        List<String> exerciseNames = new ArrayList<>();

        for (Path dir : directories) {
            if (dir.equals(getCourseDirectory())) {
                return allExerciseNames;
            }
            // convert path to a string relative to the course dir
            String exDir = getCourseDirectory().relativize(dir)
                    .toString().replace(File.separator, "-");
            for (String exercise : allExerciseNames) {
                if (exercise.startsWith(exDir)) {
                    // if the exercise starts with this path, add it
                    exerciseNames.add(exercise);
                } else if (exDir.startsWith(exercise)) {
                    // if the path contains the name of the exercise but is longer
                    // eg, if we submit from the src/ directory, add it
                    exerciseNames.add(exercise);
                    // we can break here because we know this path won't match anything else
                    break;
                }
            }
        }

        return exerciseNames;
//        if (this.configFile == null) {
//            return new ArrayList<>();
//        }
//        CourseInfo info = CourseInfoIo.load(this.configFile);
//        List<Exercise> exercises = info.getExercises();
//        List<String> exerciseNames = new ArrayList<>();
//        if (params == null || params.length == 0) {
//            if (this.workingDirectory.equals(this.courseDirectory)) {
//                // In course root dir and no params - return all exercises
//                for (Exercise exercise : exercises) {
//                    exerciseNames.add(exercise.getName());
//                }
//                return exerciseNames;
//            }
//            // If parametres are empty but we are in a subdirectory, create an array
//            // with a single element and make that element the relative subdirectory
//            params = new String[1];
//            String param = this.courseDirectory
//                    .relativize(this.workingDirectory).toString();
//            params[0] = param.replace(File.separator, "-");
//        } else {
//            for (int i = 0; i < params.length; i++) {
//                // Convert given parametres to either full exercise names
//                // Or substrings, eg. if the user is in subdirectory called "viikko1"
//                // and gives parametre "teht2", it will be converted to "viikko1-teht2"
//                String param = this.courseDirectory
//                        .relativize(this.workingDirectory.resolve(params[i])).toString();
//                params[i] = param.replace(File.separator, "-");
//            }
//        }
//        for (Exercise exercise : exercises) {
//            for (String param : params) {
//                // Match only exercises that begin with our parametres, so that
//                // exercises with identical names in other subdirectories won't
//                // be selected.
//                if (exercise.getName().matches("^" + param + ".*")) {
//                    exerciseNames.add(exercise.getName());
//                }
//            }
//        }
//        return exerciseNames;
    }

    /**
     * This can be used for operations which only use a single path.
     * If only one path has been added, return that. If no paths are added,
     * return the current working directory.
     */
    public Path getWorkingDirectory() {
        if (this.directoryCount() == 1) {
            return this.directories.get(0);
        } else if (this.directoryCount() == 0) {
            return workdir;
        }
        return null;
    }

    public List<Path> getDirectories() {
        return new ArrayList<Path>(this.directories);
    }

    /**
     * Add a path to this object's directories.
     * Return true if and only if the path is in the same course directory
     * as all other directories.
     * @param path: given path
     * @return: whether the path given is in the course directory
     */
    public boolean addPath(Path path) {
        path = makeAbsolute(path);
        if (this.directories.isEmpty()) {
            this.directories.add(path);
            this.courseDirectory = findCourseDir(path);
            if (this.courseDirectory != null) {
                this.configFile = this.courseDirectory.resolve(CourseInfoIo.COURSE_CONFIG);
                return true;
            } else {
                return false;
            }
        }
        if (!this.directories.contains(path)) {
            this.directories.add(path);
        }
        if (path.startsWith(this.courseDirectory)) {
            return true;
        }
        return false;
    }

    public boolean addPath(String path) {
        return addPath(Paths.get(path));
    }

    /**
     * Same as addPath(Path path), but adds the current working directory.
     * Note that workdir should ONLY be overridden in tests
     * Actually this is kind of useless. Remove if it remains unused.
     */
    public boolean addPath() {
        return addPath(workdir);
    }

    public int directoryCount() {
        return this.directories.size();
    }

    private Path findCourseDir(Path dir) {
        while (dir != null && Files.exists(dir)) {
            if (Files.exists(dir.resolve(CourseInfoIo.COURSE_CONFIG))) {
                return dir;
            }
            dir = dir.getParent();
        }
        return null;
    }

    private Path makeAbsolute(Path path) {
        if (path.isAbsolute()) {
            return path;
        } else {
            return workdir.resolve(path);
        }
    }

    /**
     * THIS IS ONLY FOR TESTS. DO NOT USE THIS OUTSIDE OF TESTS.
     */
    public void setWorkdir(Path path) {
        this.workdir = path;
    }
}
