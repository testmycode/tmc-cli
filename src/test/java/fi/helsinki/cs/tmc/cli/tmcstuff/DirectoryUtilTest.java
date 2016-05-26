package fi.helsinki.cs.tmc.cli.tmcstuff;

import static junit.framework.Assert.assertNull;

import org.junit.Test;

/**
 * Created by jclakkis on 26.5.2016.
 */
public class DirectoryUtilTest {
    private DirectoryUtil dirutil;

    @Test
    public void failsIfNotInCourseDirectory() {
        DirectoryUtil dirutil = new DirectoryUtil();
        assertNull(dirutil.getCourseDirectory());
        assertNull(dirutil.getConfigFile());
        assertNull(dirutil.getExerciseName());
    }
}
