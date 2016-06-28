package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.domain.Exercise;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//TODO should this be in io package?
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
     * @return: return names of exercises as List
     */
    public List<String> getExerciseNames(
            Boolean exists, Boolean onlyTested, Boolean filterCompleted) {
        if (this.directories.isEmpty()) {
            if (!addPath()) {
                return new ArrayList<>();
            }
        }
        /*TODO somehow use the ctx.getCourseInfo */
        CourseInfo courseinfo = CourseInfoIo.load(getConfigFile());
        if (courseinfo == null) {
            return new ArrayList<>();
        }
        List<Exercise> allExercises = courseinfo.getExercises();
        List<String> exerciseNames = new ArrayList<>();
        List<String> locallyTested = courseinfo.getLocalCompletedExercises();

        if (directories.contains(getCourseDirectory())) {
            for (File file : getCourseDirectory().toFile().listFiles()) {
                if (file.isDirectory()) {
                    addPath(file.toPath());
                }
            }
            directories.remove(getCourseDirectory());
        }

        for (Path dir : directories) {

            // convert path to a string relative to the course dir
            String exDir = getCourseDirectory().relativize(dir)
                    .toString();
            for (Exercise exercise : allExercises) {
                if (filterExercise(exercise, exDir, locallyTested,
                        exists, onlyTested, filterCompleted)
                        && !exDir.isEmpty()) {
                    exerciseNames.add(exercise.getName());
                }
            }
        }

        return exerciseNames;
    }

    private Boolean filterExercise(Exercise exercise, String exDir, List<String> tested,
                                   Boolean exists, Boolean onlyTested, Boolean filterCompleted) {
        // if the exercise starts with this path, add it
        if (exercise.getName().startsWith(exDir.replace(File.separator, "-"))
                || exDir.replace(File.separator, "-").startsWith(exercise.getName())) {
            if (!onlyTested || tested.contains(exercise.getName())) {
                if (!filterCompleted || !exercise.isCompleted()) {
                    if (!exists || Files.exists(courseDirectory.resolve(exDir))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This can be used for operations which only use a single path.
     * If only one path has been added, return that. If no paths are added,
     * return the current working directory.
     */
    public Path getWorkingDirectory() {
        if (this.courseDirectory != null) {
            return this.courseDirectory;
        } else {
            return workdir;
        }
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
