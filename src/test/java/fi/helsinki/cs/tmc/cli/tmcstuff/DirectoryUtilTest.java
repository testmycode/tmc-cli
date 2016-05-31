package fi.helsinki.cs.tmc.cli.tmcstuff;

import static junit.framework.Assert.assertNull;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.persistance.ConfigFileIO;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jclakkis on 26.5.2016.
 */
public class DirectoryUtilTest {
    private DirectoryUtil dirutil;

    @Before
    public void setup() {
        Path workDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        try {
            Files.createDirectories(workDir);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
        try {
            Files.createDirectories(workDir.resolve("viikko1").resolve("teht1"));
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
        try {
            Files.createDirectories(workDir.resolve("viikko2").resolve("teht2"));
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
        try {
            Files.createDirectories(workDir.resolve("viikko2").resolve("subdir").resolve("teht3"));
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
        List<Exercise> exercises = new ArrayList<Exercise>();
        exercises.add(new Exercise("viikko1-teht1"));
        exercises.add(new Exercise("viikko2-teht2"));
        exercises.add(new Exercise("viikko2-subdir-teht3"));
        CourseInfo info = new CourseInfo(new Settings(true), "dirUtilTest");
        CourseInfoIo infoio = new CourseInfoIo(workDir.resolve(CourseInfoIo.COURSE_CONFIG));
    }

    @After
    public void cleanUp() {
        String tempDir = System.getProperty("java.io.tmpdir");
        try {
            FileUtils.deleteDirectory(Paths.get(tempDir).resolve("dirUtilTest").toFile());
        } catch (Exception e) { }
    }

    @Test
    public void failsIfNotInCourseDirectory() {
        DirectoryUtil dirutil = new DirectoryUtil(
                Paths.get(System.getProperty("java.io.tmpdir"), null));
        assertNull(dirutil.getCourseDirectory());
        assertNull(dirutil.getConfigFile());
        assertNull(dirutil.getExerciseName());
    }

    @Test
    public void worksIfInCourseDirectoryWithNoParams() {
        Path workDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        DirectoryUtil dirutil = new DirectoryUtil(workDir, null);
        Assert.assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest").resolve(CourseInfoIo.COURSE_CONFIG),
                dirutil.getConfigFile());
        List<String> exercises = dirutil.getExerciseNames(new String[] {});
        Assert.assertTrue(exercises.contains("viikko1-teht1"));
        Assert.assertTrue(exercises.contains("viikko2-teht2"));
        Assert.assertTrue(exercises.contains("viikko2-subdir-teht3"));
    }

    @Test
    public void worksIfInSubDirectoryWithNoParams() {
        Path workDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest")
                .resolve("viikko2");
        DirectoryUtil dirutil = new DirectoryUtil(workDir, null);
        Assert.assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest").resolve(CourseInfoIo.COURSE_CONFIG),
                dirutil.getConfigFile());
        List<String> exercises = dirutil.getExerciseNames(new String[] {});
        Assert.assertFalse(exercises.contains("viikko1-teht1"));
        Assert.assertTrue(exercises.contains("viikko2-teht2"));
        Assert.assertTrue(exercises.contains("viikko2-subdir-teht3"));
    }

    @Test
    public void worksIfInSubSubDirectoryWithNoParams() {
        Path workDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest")
                .resolve("viikko2").resolve("subdir");
        DirectoryUtil dirutil = new DirectoryUtil(workDir, null);
        Assert.assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest").resolve(CourseInfoIo.COURSE_CONFIG),
                dirutil.getConfigFile());
        List<String> exercises = dirutil.getExerciseNames(new String[] {});
        Assert.assertFalse(exercises.contains("viikko1-teht1"));
        Assert.assertFalse(exercises.contains("viikko2-teht2"));
        Assert.assertTrue(exercises.contains("viikko2-subdir-teht3"));
    }

    @Test
    public void worksIfInCourseDirectoryWithParams() {
        Path workDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest");
        DirectoryUtil dirutil = new DirectoryUtil(workDir, null);
        Assert.assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest").resolve(CourseInfoIo.COURSE_CONFIG),
                dirutil.getConfigFile());
        List<String> exercises = dirutil.getExerciseNames(new String[] {"viikko2"});
        Assert.assertFalse(exercises.contains("viikko1-teht1"));
        Assert.assertTrue(exercises.contains("viikko2-teht2"));
        Assert.assertTrue(exercises.contains("viikko2-subdir-teht3"));
        exercises = dirutil.getExerciseNames(new String[] {"teht"});
        Assert.assertFalse(exercises.contains("viikko1-teht1"));
        Assert.assertFalse(exercises.contains("viikko2-teht2"));
        Assert.assertFalse(exercises.contains("viikko2-subdir-teht3"));
    }

    @Test
    public void worksIfInSubDirectoryWithParams() {
        Path workDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest")
                .resolve("viikko2");
        DirectoryUtil dirutil = new DirectoryUtil(workDir, null);
        Assert.assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest").resolve(CourseInfoIo.COURSE_CONFIG),
                dirutil.getConfigFile());
        List<String> exercises = dirutil.getExerciseNames(new String[] {"teht"});
        Assert.assertFalse(exercises.contains("viikko1-teht1"));
        Assert.assertTrue(exercises.contains("viikko2-teht2"));
        Assert.assertFalse(exercises.contains("viikko2-subdir-teht3"));
    }

    @Test
    public void worksIfInSubSubDirectoryWithParams() {
        Path workDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("dirUtilTest")
                .resolve("viikko2").resolve("subdir");
        DirectoryUtil dirutil = new DirectoryUtil(workDir, null);
        Assert.assertEquals(
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve("dirUtilTest").resolve(CourseInfoIo.COURSE_CONFIG),
                dirutil.getConfigFile());
        List<String> exercises = dirutil.getExerciseNames(new String[] {"teht"});
        Assert.assertFalse(exercises.contains("viikko1-teht1"));
        Assert.assertFalse(exercises.contains("viikko2-teht2"));
        Assert.assertTrue(exercises.contains("viikko2-subdir-teht3"));
    }
}
