package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfo;
import fi.helsinki.cs.tmc.cli.tmcstuff.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;

import java.util.HashMap;

public class CliContext {

    private final WorkDir workDir;
    private final Io io;

    private Application application;
    private TmcCore tmcCore;
    private Settings settings;

    /* cached values */
    private boolean hasLogin;
    private CourseInfo courseInfo;
    private HashMap<String, String> properties;
    private final boolean inTest;

    public CliContext(Io io) {
        this(io, null);
    }

    public CliContext(Io io, TmcCore core) {
        this(io, core, new WorkDir());
    }

    public CliContext(Io io, TmcCore core, WorkDir workDir) {
        inTest = (io != null);
        if (!inTest) {
            io = new TerminalIo(System.in);
        }
        // This is only used when we want to mock the tmc core.
        if (core != null) {
            this.settings = new Settings();
        }

        this.io = io;
        this.workDir = workDir;
        this.properties = SettingsIo.loadProperties();
        this.tmcCore = core;
        this.hasLogin = false;
        this.courseInfo = null;
    }

    /*TODO create reset method for removing all cached data that is called
     * when working directory is changed or by users demand also use it in
     * constructor.
     */

    protected void setApp(Application app) {
        this.application = app;
    }

    public Application getApp() {
        if (application == null) {
            throw new RuntimeException("Application isn't usually set in tests.");
        }
        return application;
    }

    //TODO get rid of this (use properties)
    @Deprecated
    public boolean inTests() {
        return inTest;
    }

    public Io getIo() {
        return io;
    }

    public WorkDir getWorkDir() {
        return this.workDir;
    }

    public CourseInfo createCourseInfo(Course course) {
        return new CourseInfo(settings, course);
    }

    public HashMap<String, String> getProperties() {
        // Loads properties from the global configuration file in .config/tmc-cli/
        return this.properties;
    }

    public Boolean saveProperties() {
        // Saves properties to the global configuration file in .config/tmc-cli/
        return SettingsIo.saveProperties(properties);
    }

    public CourseInfo getCourseInfo() {
        if (courseInfo != null) {
            return courseInfo;
        }
        if (workDir.getConfigFile() == null) {
            return null;
        }
        courseInfo = CourseInfoIo.load(workDir.getConfigFile());
        if (courseInfo == null) {
            io.println("Course configuration file "
                    + workDir.getConfigFile().toString()
                    + "is invalid.");
            //TODO add a way to rewrite the course config file.
            return null;
        }
        return this.courseInfo;
    }

    // Method is used to help testing
    public void setTmcCore(TmcCore tmcCore) {
        this.tmcCore = tmcCore;
    }

    public boolean loadBackend() {
        return loadBackend(true);
    }

    public boolean loadBackend(boolean useInternet) {
        if (this.tmcCore != null) {
            return true;
        }

        if (!createTmcCore()) {
            return false;
        }

        if (useInternet && !hasLogin) {
            // If no settings are present
            if (courseInfo == null) {
                io.println("You are not logged in. Log in using: tmc login");
            } else {
                io.println("You are not logged in as " + courseInfo.getUsername()
                        + ". Log in using: tmc login");
            }
            return false;
        }
        return true;
    }

    public TmcCore getTmcCore() {
        if (this.tmcCore == null) {
            throw new RuntimeException("The loadBackend method was NOT called");
        }
        return this.tmcCore;
    }

    public void useSettings(Settings settings) {
        if (this.tmcCore == null) {
            createTmcCore(settings);
        }
        this.settings.set(settings);
    }

    private void createTmcCore(Settings settings) {
        TaskExecutor tmcLangs;

        tmcLangs = new TaskExecutorImpl();
        this.settings = settings;
        this.tmcCore = new TmcCore(settings, tmcLangs);
        settings.setWorkDir(workDir);
    }

    private boolean createTmcCore() {
        Settings cachedSettings = null;

        if (workDir.getConfigFile() != null) {
            // If we're in a course directory, we load settings matching the course
            // Otherwise we just load the last used settings
            courseInfo = getCourseInfo();
            if (courseInfo != null) {
                cachedSettings = SettingsIo.load(courseInfo.getUsername(),
                        courseInfo.getServerAddress());
            }
        } else {
            cachedSettings = SettingsIo.load();
        }

        hasLogin = true;
        if (cachedSettings == null) {
            hasLogin = false;
            cachedSettings = new Settings();
        }

        createTmcCore(cachedSettings);
        return true;
    }
}
