package fi.helsinki.cs.tmc.cli.command;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TmcUtil.class, SettingsIo.class})
public class ListCoursesCommandTest {

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;

    @Before
    public void setUp() {
        io = new TestIo();
        mockCore = mock(TmcCore.class);
        ctx = new CliContext(io, mockCore);
        app = new Application(ctx);
        Settings settings = new Settings("http://test.test", "", "");

        mockStatic(TmcUtil.class);
        mockStatic(SettingsIo.class);
        when(SettingsIo.getSettingsList()).thenReturn(Arrays.asList(settings));
    }

    @Test
    public void failIfBackendFails() {
        CliContext ctx = spy(new CliContext(io, mockCore));
        app = new Application(ctx);
        doReturn(false).when(ctx).loadBackend();

        String[] args = {"courses", "foo"};
        app.run(args);
        io.assertNotContains("Course doesn't exist");
    }

    @Test
    public void listCoursesWorksWithNoCourses() {
        List<Course> list = Arrays.asList();
        when(TmcUtil.listCourses(eq(ctx))).thenReturn(list);

        String[] args = {"courses"};
        app.run(args);
        io.assertContains("No courses found");
    }

    @Test
    public void listCoursesWorksWithCourses() {
        List<Course> list = Arrays.asList(new Course("course1"), new Course("course2"));
        when(TmcUtil.listCourses(eq(ctx))).thenReturn(list);

        String[] args = {"courses"};
        app.run(args);
        io.assertContains("Found 2 courses");
    }

    @Test
    public void listCoursesWorksWithTwoServers() {
        Settings settings1 = new Settings("http://test.test", "", "");
        Settings settings2 = new Settings("http://hello.test", "", "");
        when(SettingsIo.getSettingsList()).thenReturn(
                Arrays.asList(settings1, settings2));

        List<Course> list1 = Arrays.asList(new Course("course1"));
        List<Course> list2 = Arrays.asList(new Course("course2"));
        when(TmcUtil.listCourses(eq(ctx))).thenReturn(list1).thenReturn(list2);

        String[] args = {"courses"};
        app.run(args);
        io.assertContains("Server http://test.test");
        io.assertContains("Server http://hello.test");
    }
}
