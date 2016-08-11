package fi.helsinki.cs.tmc.cli.io;

import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;
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

    public Path getTmcDirectory() {
        Path path = getCourseDirectory();
        if (path == null) {
            return this.workdir;
        }
        return path.getParent();
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

    public List<String> getExerciseNames() {
        return getExerciseNames(true, false, false);
    }

    /**
     * Go through directories and return matching exercises.
     * @param exists Returns only exercises that aren't removed
     * @param onlyTested Return only exercises that are already tested
     * @param filterCompleted Remove the ones that are already completed
     * @return: return names of exercises as List
     */
    public List<String> getExerciseNames(
            boolean exists, boolean onlyTested, boolean filterCompleted) {
        if (this.directories.isEmpty() && getConfigFile() == null) {
            return new ArrayList<>();
        }
        /*TODO somehow use the ctx.getCourseInfo */
        CourseInfo courseinfo = CourseInfoIo.load(getConfigFile());
        if (courseinfo == null) {
            return new ArrayList<>();
        }
        List<Exercise> allExercises = courseinfo.getExercises();
        List<Exercise> exercises = new ArrayList<>();
        List<String> filteredExerciseNames = new ArrayList<>();
        List<String> locallyTested = courseinfo.getLocalCompletedExercises();

        // Conditions for adding all exercises:
        //   course dir given as parameter
        //   working dir is course dir and no parameters
        if (directories.contains(getCourseDirectory())
                || (getCourseDirectory().equals(workdir) && directories.size() == 0)) {
            exercises.addAll(allExercises);
        } else if (workdir.startsWith(getCourseDirectory())
                && !workdir.equals(getCourseDirectory())) {
            // if workdir is an exercise directory, add it to directories
            directories.add(workdir);
        } else if (getCourseDirectory() == null) {
            // if we still don't have a course directory, return an empty list
            return new ArrayList<>();
        }

        for (Path dir : directories) {
            // convert path to a string relative to the course dir
            String exDir = getCourseDirectory().relativize(dir).toString();
            for (Exercise exercise : allExercises) {
                if ((exercise.getName().startsWith(exDir.replace(File.separator, "-"))
                        || exDir.replace(File.separator, "-").startsWith(exercise.getName()))
                        && !exercises.contains(exercise)) {
                    exercises.add(exercise);
                }
            }
        }

        for (Exercise exercise : exercises) {
            if (filterExercise(exercise, locallyTested, exists, onlyTested, filterCompleted)) {
                filteredExerciseNames.add(exercise.getName());
            }
        }

        return filteredExerciseNames;
    }

    private boolean filterExercise(Exercise exercise, List<String> tested,
            boolean exists, boolean onlyTested, boolean filterCompleted) {
        if (onlyTested && !tested.contains(exercise.getName())) {
            return false;
        }
        if (filterCompleted && exercise.isCompleted()) {
            return false;
        }
        if (exists && !Files.exists(getCourseDirectory().resolve(exercise.getName()))) {
            return false;
        }
        return true;
    }

    /**
     * THIS IS ONLY FOR TESTS. DO NOT USE THIS OUTSIDE OF TESTS.
     */
    public void setWorkdir(Path path) {
        this.workdir = path;
    }

    /**
     * Get the working directory.
     */
    public Path getWorkingDirectory() {
        return workdir;
    }

    public List<Path> getDirectories() {
        return new ArrayList<>(this.directories);
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
            if (isCourseDirectory(dir)) {
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

    private boolean isCourseDirectory(Path dir) {
        return Files.exists(dir.resolve(CourseInfoIo.COURSE_CONFIG));
    }
}
