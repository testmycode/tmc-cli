package fi.helsinki.cs.tmc.cli.backend;

import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class for reading and writing to course config files (.tmc.json)
 */
public class CourseInfoIo {

    private static final Logger logger = LoggerFactory.getLogger(SettingsIo.class);

    // COURSE_CONFIG is the _local_ configuration file containing course
    // information and is located in the root of each different course.
    // Contains username, server and course name.
    public static final String COURSE_CONFIG = ".tmc.json";

    public static Boolean save(CourseInfo course, Path courseInfoFile) {
        Gson gson = new Gson();
        byte[] json = gson.toJson(course).getBytes();
        try {
            Files.createDirectories(courseInfoFile.getParent());
            Files.write(courseInfoFile, json);
        } catch (IOException e) {
            //TODO print to user
            logger.error("Could not create course file", e);
            return false;
        }
        return true;
    }

    public static CourseInfo load(Path courseInfoFile) {
        Gson gson = new Gson();
        if (!Files.exists(courseInfoFile)) {
            //Return null if file is not found, this is normal behaviour
            return null;
        }
        Reader reader;
        try {
            reader = Files.newBufferedReader(courseInfoFile, Charset.forName("UTF-8"));
        } catch (IOException e) {
            //TODO print to user
            logger.error("Course file located, but failed to read from it", e);
            return null;
        }
        return gson.fromJson(reader, CourseInfo.class);
    }

    public static void createNewCourse(Course course, Account account, Path parentDir) {
        Path configFile = parentDir.resolve(course.getName()).resolve(CourseInfoIo.COURSE_CONFIG);

        CourseInfo info = new CourseInfo(account, course);
        info.setExercises(course.getExercises());
        CourseInfoIo.save(info, configFile);
    }

    public static void deleteConfigDirectory(Course course, Path parentDir) {
        Path configFile = parentDir.resolve(course.getName()).resolve(CourseInfoIo.COURSE_CONFIG);
        delete(configFile);
    }

    private static void delete(Path courseInfoFile) {
        try {
            Files.deleteIfExists(courseInfoFile);
        } catch (IOException e) {
            logger.error("Could not delete course file", e);
        }
    }
}
