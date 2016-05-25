package fi.helsinki.cs.tmc.cli.tmcstuff;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class for reading and writing to course config files (.tmc.json)
 */
public class CourseInfoIo {

    private static final Logger logger = LoggerFactory.getLogger(SettingsIo.class);

    // COURSE_CONFIG is the _local_ configuration file containing course
    // information and is located in the root of each different course.
    // Contains username, server and course name.
    public static final String COURSE_CONFIG = ".tmc.json";

    private Path courseInfoFile;

    public CourseInfoIo(Path courseInfoFile) {
        this.courseInfoFile = courseInfoFile;
    }


    public Boolean save(CourseInfo course) {
        Path file = this.courseInfoFile;
        Gson gson = new Gson();
        byte[] json = gson.toJson(course).getBytes();
        try {
            Files.write(file, json);
        } catch (IOException e) {
            logger.error("Could not create course file", e);
            return false;
        }
        return true;
    }

    public CourseInfo load() {
        Path file = this.courseInfoFile;
        Gson gson = new Gson();
        if (!Files.exists(file)) {
            //Return null if file is not found, this is normal behaviour
            return null;
        }
        Reader reader = null;
        try {
            reader = Files.newBufferedReader(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            logger.error("Course file located, but failed to read from it", e);
            return null;
        }
        return gson.fromJson(reader, CourseInfo.class);
    }
}
