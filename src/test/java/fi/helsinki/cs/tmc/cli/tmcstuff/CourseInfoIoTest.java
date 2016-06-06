package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.domain.Course;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jclakkis on 25.5.2016.
 */
public class CourseInfoIoTest {
    private Settings settings;
    private CourseInfo course;
    private CourseInfoIo courseio;
    private Path courseFile;

    @Before
    public void setup() {
        String tempDir = System.getProperty("java.io.tmpdir");
        this.courseFile = Paths.get(tempDir)
                .resolve("test course")
                .resolve(".tmc.json");
        this.settings = new Settings();
        this.course = new CourseInfo(this.settings, new Course("test-course"));
        this.courseio = new CourseInfoIo(this.courseFile);
        try {
            FileUtils.deleteDirectory(Paths.get(tempDir)
                    .resolve("test course").toFile());
        } catch (Exception e) { }
    }

    @After
    public void cleanUp() {
        String tempDir = System.getProperty("java.io.tmpdir");
        try {
            FileUtils.deleteDirectory(Paths.get(tempDir)
                    .resolve("test course").toFile());
        } catch (Exception e) { }
    }

    @Test
    public void savingToFileWorks() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Boolean success = this.courseio.save(this.course);

        Assert.assertTrue(success);
        Assert.assertTrue(Files.exists(Paths.get(tempDir)
                .resolve("test course").resolve(".tmc.json")));
    }

    @Test
    public void loadingFromFileWorks() {
        String tempDir = System.getProperty("java.io.tmpdir");
        this.courseio.save(this.course);

        CourseInfo loadedInfo = this.courseio.load();
        Assert.assertEquals(this.course.getServerAddress(), loadedInfo.getServerAddress());
        Assert.assertEquals(this.course.getUsername(), loadedInfo.getUsername());
        Assert.assertEquals(this.course.getCourseName(), loadedInfo.getCourseName());
    }

}
