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

    private WorkDir workDir;//TODO make this final
    private final Io io;

    private Application application;
    private TmcCore tmcCore;
    private Settings settings;

    private HashMap<String, String> properties;
    private final boolean inTest;

    /*TODO some of the constructors could be removed */
    public CliContext(Io io) {
        this(io, new WorkDir());
    }

    public CliContext(Io io, TmcCore core) {
        this(io, new WorkDir(), core);
    }

    @Deprecated
    public CliContext(Io io, WorkDir workDir) {
        this(io, workDir, null);
    }

    public CliContext(Io io, WorkDir workDir, TmcCore core) {
        inTest = (io != null);
        if (!inTest) {
            io = new TerminalIo();
        }
        // This is only used when we want to mock the tmc core.
        if (core != null) {
            this.settings = new Settings();
        }

        this.io = io;
        this.workDir = workDir;
        this.properties = SettingsIo.loadProperties();
        this.tmcCore = core;
    }

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
    public boolean inTests() {
        return inTest;
    }

    public Io getIo() {
        return io;
    }

    public WorkDir getWorkDir() {
        return this.workDir;
    }

    /**
     * Does some thing in old style.
     *
     * @deprecated use {@link fi.helsinki.cs.tmc.cli.tmcstuff.WorkDir#setWorkdir(String)} instead.
     */
    @Deprecated
    public void setWorkdir(WorkDir workDir) {
        this.workDir = workDir;
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

        return createTmcCore();
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

    public void restoreSettings() {
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
        SettingsIo settingsio = new SettingsIo();
        Settings settings;

        if (workDir.getConfigFile() != null) {
            // If we're in a course directory, we load settings matching the course
            // Otherwise we just load the last used settings
            CourseInfo courseinfo = CourseInfoIo.load(workDir.getConfigFile());
            if (courseinfo == null) {
                //io.println("Course configuration file "
                //        + workDir.getConfigFile().toString()
                //        + "is invalid.");
                //return false;
                createTmcCore(new Settings());
                return true;
            }
            settings = settingsio.load(courseinfo.getUsername(),
                    courseinfo.getServerAddress());
        } else {
            settings = settingsio.load();
        }

        if (settings == null) {
            // If no settings are present
            io.println("You are not logged in. Log in using: tmc login");
            return false;
        }
        createTmcCore(settings);
        return true;
    }
}
