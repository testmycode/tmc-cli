package fi.helsinki.cs.tmc.cli.io;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;

import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WorkDirTest {

    public static Path TEST_DIR;
    public static Exercise exercise1;
    public static Exercise exercise2;
    public static Exercise exercise3;
    public static Exercise nonexistentExercise;

    @BeforeClass
    public static void setup() {
        TEST_DIR = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        try {
            Files.createDirectories(TEST_DIR);
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            Files.createDirectories(TEST_DIR.resolve("viikko1-teht1").resolve("src"));
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            Files.createDirectories(TEST_DIR.resolve("viikko2-teht2").resolve("src"));
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            Files.createDirectories(TEST_DIR.resolve("viikko2-teht3").resolve("src"));
        } catch (Exception e) {
            fail(e.toString());
        }
        exercise1 = new Exercise("viikko1-teht1");
        exercise2 = new Exercise("viikko2-teht2");
        exercise3 = new Exercise("viikko2-teht3");
        nonexistentExercise = new Exercise("viikko3-nonexistent");
        exercise2.setCompleted(true);

        List<Exercise> exercises = new ArrayList<>();
        exercises.add(exercise1);
        exercises.add(exercise2);
        exercises.add(exercise3);
        exercises.add(nonexistentExercise);
        CourseInfo info = new CourseInfo(new Account(), new Course("dirUtilTest"));
        info.getLocalCompletedExercises().add("viikko1-teht1");
        info.setExercises(exercises);
        CourseInfoIo.save(info, TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG));
    }

    @AfterClass
    public static void cleanUp() {
        try {
            FileUtils.deleteDirectory(TEST_DIR.toFile());
        } catch (Exception e) { }
    }

    @Test
    public void getsCorrectWorkingDirectory() {
        WorkDir workDir = new WorkDir();
        assertNotNull(workDir.getWorkingDirectory());
        assertEquals(Paths.get(System.getProperty("user.dir")),
                workDir.getWorkingDirectory());
    }

    @Test
    public void overridingWorkingDirectoryWorks() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        assertEquals(TEST_DIR, workDir.getWorkingDirectory());
    }

    @Test
    public void failsIfNotInCourseDirectory() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(Paths.get(System.getProperty("java.io.tmpdir")));
        assertNull(workDir.getCourseDirectory());
        assertNull(workDir.getConfigFile());
        assertTrue("Returns no exercise names", workDir.getExercises().isEmpty());
    }

    @Test
    public void absolutePathsWork() {
        WorkDir workDir = new WorkDir();
        workDir.addPath(TEST_DIR);
        assertEquals(TEST_DIR, workDir.getCourseDirectory());
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
    }

    @Test
    public void returnsCorrectValuesInCourseDirectory() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        assertEquals("Course dir is correct", TEST_DIR,
                workDir.getCourseDirectory());
        assertEquals("Working dir is correct", TEST_DIR,
                workDir.getWorkingDirectory());
    }

    @Test
    public void returnsCorrectValuesInExerciseDirectory() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR.resolve("viikko1-teht1"));
        assertEquals(TEST_DIR, workDir.getCourseDirectory());
    }

    @Test
    public void worksIfInCourseDirectoryWithNoParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<Exercise> exercises = workDir.getExercises(true, false);
        assertEquals(3, exercises.size());
        assertTrue(exercises.contains(exercise1));
        assertTrue(exercises.contains(exercise2));
        assertTrue(exercises.contains(exercise3));
    }

    @Test
    public void worksIfCourseDirectoryIsGivenAsAParameter() {
        WorkDir workDir = new WorkDir();
        workDir.addPath(TEST_DIR);
        assertEquals(TEST_DIR, workDir.getCourseDirectory());
    }

    @Test
    public void worksIfInExerciseDirectoryWithNoParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR.resolve("viikko2-teht2"));
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<Exercise> exercises = workDir.getExercises(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains(exercise1));
        assertTrue(exercises.contains(exercise2));
        assertFalse(exercises.contains(exercise3));
    }

    @Ignore // Obsolete functionality
    @Test
    public void worksIfInSubSubDirectoryWithNoParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR.resolve("viikko2").resolve("subdir"));
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<Exercise> exercises = workDir.getExercises(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains(exercise1));
        assertFalse(exercises.contains(exercise2));
        assertTrue(exercises.contains(exercise3));
    }

    @Test
    public void addInvalidPath() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        workDir.addPath("teht1");
        List<Exercise> exercises = workDir.getExercises();
        assertEquals(0, exercises.size());
    }

    @Test
    public void addTwoExercises() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        workDir.addPath("viikko2-teht2");
        workDir.addPath("viikko2-teht3");
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<Exercise> exercises = workDir.getExercises();
        assertEquals(2, exercises.size());
        assertFalse(exercises.contains(exercise1));
        assertTrue(exercises.contains(exercise2));
        assertTrue(exercises.contains(exercise3));
    }

    @Test
    public void addSamePathTwice() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        workDir.addPath("viikko2-teht2");
        workDir.addPath("viikko2-teht2");
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<Exercise> exercises = workDir.getExercises();
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains(exercise1));
        assertTrue(exercises.contains(exercise2));
        assertFalse(exercises.contains(exercise3));
    }

    @Ignore // Obsolete functionality
    @Test
    public void worksIfInSubDirectoryWithParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR.resolve("viikko2"));
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        workDir.addPath("teht2");
        List<Exercise> exercises = workDir.getExercises(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains(exercise1));
        assertTrue(exercises.contains(exercise2));
        assertFalse(exercises.contains(exercise3));
    }

    @Ignore // Obsolete functionality
    @Test
    public void worksIfInSubSubDirectoryWithParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR.resolve("viikko2").resolve("subdir"));
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest").resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        workDir.addPath("teht3");
        List<Exercise> exercises = workDir.getExercises(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains(exercise1));
        assertFalse(exercises.contains(exercise2));
        assertTrue(exercises.contains(exercise3));
    }

    @Test
    public void worksInSubDirectoryOfAnExercise() {
        Path path = TEST_DIR.resolve("viikko1-teht1").resolve("src");
        WorkDir workDir = new WorkDir();
        workDir.addPath(path);
        List<Exercise> exercises = workDir.getExercises(true, false);
        assertEquals(1, exercises.size());
        assertTrue(exercises.contains(exercise1));
        assertFalse(exercises.contains(exercise2));
        assertFalse(exercises.contains(exercise3));
    }

    @Test
    public void worksWhenDeletedExercisesAreNotFiltered() {
        WorkDir workDir = new WorkDir();
        workDir.addPath(TEST_DIR);
        List<Exercise> exercises = workDir.getExercises(false, false);
        assertEquals(4, exercises.size());
        assertTrue(exercises.contains(exercise1));
        assertTrue(exercises.contains(exercise2));
        assertTrue(exercises.contains(exercise3));
        assertTrue(exercises.contains(nonexistentExercise));
    }
}
