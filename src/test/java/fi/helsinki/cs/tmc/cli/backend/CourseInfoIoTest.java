package fi.helsinki.cs.tmc.cli.backend;

import fi.helsinki.cs.tmc.core.domain.Course;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CourseInfoIoTest {

    private CourseInfo course;
    private Path courseFile;
    private String tempDir;

    @Before
    public void setup() {
        tempDir = System.getProperty("java.io.tmpdir");
        this.courseFile = Paths.get(tempDir)
                .resolve("test-course")
                .resolve(CourseInfoIo.COURSE_CONFIG);
        this.course = new CourseInfo(new Account(), new Course("test-course"));
    }

    @After
    public void cleanUp() {
        try {
            FileUtils.deleteDirectory(Paths.get(tempDir)
                    .resolve("test-course").toFile());
        } catch (Exception e) { }
    }

    @Test
    public void savingToFileWorks() {
        Boolean success = CourseInfoIo.save(this.course, this.courseFile);
        Assert.assertTrue(success);
        Assert.assertTrue(Files.exists(this.courseFile));
    }

    @Test
    public void loadingFromFileWorks() {
        CourseInfoIo.save(this.course, this.courseFile);

        CourseInfo loadedInfo = CourseInfoIo.load(this.courseFile);
        Assert.assertNotNull(loadedInfo);
        Assert.assertEquals(this.course.getServerAddress(), loadedInfo.getServerAddress());
        Assert.assertEquals(this.course.getUsername(), loadedInfo.getUsername());
        Assert.assertEquals(this.course.getCourseName(), loadedInfo.getCourseName());
    }

}
