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

    @BeforeClass
    public static void setup() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        try {
            Files.createDirectories(tempDir);
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            Files.createDirectories(tempDir.resolve("viikko1-teht1").resolve("src"));
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            Files.createDirectories(tempDir.resolve("viikko2-teht2").resolve("src"));
        } catch (Exception e) {
            fail(e.toString());
        }
        try {
            Files.createDirectories(tempDir.resolve("viikko2-teht3").resolve("src"));
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
        CourseInfoIo.save(info, tempDir.resolve(CourseInfoIo.COURSE_CONFIG));
    }

    @AfterClass
    public static void cleanUp() {
        String tempDir = System.getProperty("java.io.tmpdir");
        try {
            FileUtils.deleteDirectory(Paths.get(tempDir).resolve("dirUtilTest").toFile());
        } catch (Exception e) {
        }
    }

    @Test
    public void getsCorrectWorkingDirectory() {
        WorkDir workDir = new WorkDir();
        assertNotNull(workDir.getWorkingDirectory());
        assertEquals(Paths.get(System.getProperty("user.dir")), workDir.getWorkingDirectory());
    }

    @Test
    public void overridingWorkingDirectoryWorks() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(Paths.get(System.getProperty("java.io.tmpdir")));
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir")), workDir.getWorkingDirectory());
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
        workDir.addPath(Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"));
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"),
                workDir.getCourseDirectory());
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest")
                        .resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
    }

    @Test
    public void returnsCorrectValuesInCourseDirectory() {
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"));
        assertEquals(
                "Course dir is correct",
                Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"),
                workDir.getCourseDirectory());
        assertEquals(
                "Working dir is correct",
                Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"),
                workDir.getWorkingDirectory());
    }

    @Test
    public void returnsCorrectValuesInExerciseDirectory() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(tempDir.resolve("viikko1-teht1"));
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"),
                workDir.getCourseDirectory());
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest")
                        .resolve("viikko1-teht1"),
                workDir.getWorkingDirectory());
    }

    @Test
    public void worksIfInCourseDirectoryWithNoParams() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(tempDir);
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest")
                        .resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<String> exercises = workDir.getExerciseNames(true, false, false);
        assertEquals(3, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-teht3"));
    }

    @Test
    public void worksIfCourseDirectoryIsGivenAsAParameter() {
        WorkDir workDir = new WorkDir();
        workDir.addPath(Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"));
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"),
                workDir.getCourseDirectory());
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest"),
                workDir.getWorkingDirectory());
    }

    @Test
    public void worksIfInExerciseDirectoryWithNoParams() {
        Path tempDir =
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest")
                        .resolve("viikko2-teht2");
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(tempDir);
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest")
                        .resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<String> exercises = workDir.getExerciseNames(true, false, false);
        assertEquals(1, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-teht3"));
    }

    @Test
    public void worksIfInCourseDirectoryWithParams() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        WorkDir workDir = new WorkDir();
        workDir.setWorkdir(tempDir);
        workDir.addPath("viikko2-teht2");
        workDir.addPath("viikko2-teht3");
        assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest")
                        .resolve(CourseInfoIo.COURSE_CONFIG),
                workDir.getConfigFile());
        List<String> exercises = workDir.getExerciseNames();
        assertEquals(2, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-teht3"));
        workDir = new WorkDir();
        workDir.setWorkdir(tempDir);
        workDir.addPath("teht1");
        exercises = workDir.getExerciseNames(true, false, false);
        assertEquals(0, exercises.size());
        assertFalse(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-teht3"));
    }

    @Test
    public void worksInSubDirectoryOfAnExercise() {
        Path path =
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest")
                        .resolve("viikko1-teht1")
                        .resolve("src");
        WorkDir workDir = new WorkDir();
        workDir.addPath(path);
        List<String> exercises = workDir.getExerciseNames(true, false, false);
        assertEquals(1, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-teht3"));
    }

    @Test
    public void worksWhenDeletedExercisesAreNotFiltered() {
        Path path = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        WorkDir workDir = new WorkDir();
        workDir.addPath(path);
        List<String> exercises = workDir.getExerciseNames(false, false, false);
        assertEquals(4, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertTrue(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-teht3"));
        assertTrue(exercises.contains("viikko3-nonexistent"));
    }

    @Test
    public void filteringCompletedExercisesWorks() {
        Path path = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        WorkDir workDir = new WorkDir();
        workDir.addPath(path);
        List<String> exercises = workDir.getExerciseNames(false, false, true);
        assertEquals(3, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertTrue(exercises.contains("viikko2-teht3"));
        assertTrue(exercises.contains("viikko3-nonexistent"));
    }

    @Test
    public void filteringOnlyTestedExercisesWorks() {
        Path path = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        WorkDir workDir = new WorkDir();
        workDir.addPath(path);
        List<String> exercises = workDir.getExerciseNames(false, true, false);
        assertEquals(1, exercises.size());
        assertTrue(exercises.contains("viikko1-teht1"));
        assertFalse(exercises.contains("viikko2-teht2"));
        assertFalse(exercises.contains("viikko2-teht3"));
        assertFalse(exercises.contains("viikko3-nonexistent"));
    }
}
