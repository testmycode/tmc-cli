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
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("viikko1-teht1"));
        exercises.add(new Exercise("viikko2-teht2"));
        exercises.get(1).setCompleted(true);
        exercises.add(new Exercise("viikko2-teht3"));
        exercises.add(new Exercise("viikko3-nonexistent"));
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
        assertTrue("Returns no exercise names", workDir.getExerciseNames().isEmpty());
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
        List<String> exercises = workDir.getExerciseNames(true, false);
        assertEquals(3, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-teht3"));
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
        List<String> exercises = workDir.getExerciseNames(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-teht3"));
    }

    @Ignore // Obsolete functionality
    @Test
    public void worksIfInSubSubDirectoryWithNoParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR.resolve("viikko2").resolve("subdir"));
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<String> exercises = workDir.getExerciseNames(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-subdir-teht3"));
    }

    @Test
    public void worksIfInCourseDirectoryWithParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        workDir.addPath("viikko2-teht2");
        workDir.addPath("viikko2-teht3");
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<String> exercises = workDir.getExerciseNames();
        assertEquals(2, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-teht3"));
        workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR);
        workDir.addPath("teht1");
        exercises = workDir.getExerciseNames(true, false);
        assertEquals(0, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-teht3"));
    }

    @Ignore // Obsolete functionality
    @Test
    public void worksIfInSubDirectoryWithParams() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(TEST_DIR.resolve("viikko2"));
        assertEquals(TEST_DIR.resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        workDir.addPath("teht2");
        List<String> exercises = workDir.getExerciseNames(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-subdir-teht3"));
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
        List<String> exercises = workDir.getExerciseNames(true, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-subdir-teht3"));
    }

    @Test
    public void worksInSubDirectoryOfAnExercise() {
        Path path = TEST_DIR.resolve("viikko1-teht1").resolve("src");
        WorkDir workDir = new WorkDir();
        workDir.addPath(path);
        List<String> exercises = workDir.getExerciseNames(true, false);
        assertEquals(1, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-teht3"));
    }

    @Test
    public void worksWhenDeletedExercisesAreNotFiltered() {
        WorkDir workDir = new WorkDir();
        workDir.addPath(TEST_DIR);
        List<String> exercises = workDir.getExerciseNames(false, false);
        assertEquals(4, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-teht3"));
        assertTrue(exercises.contains("viikko3-nonexistent"));
    }
}
