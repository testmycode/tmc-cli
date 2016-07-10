package fi.helsinki.cs.tmc.cli.core;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TerminalIo;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

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

        this.io = io;
        this.workDir = workDir;
        this.properties = SettingsIo.loadProperties();
        this.settings = new Settings();
        this.tmcCore = core;
        this.hasLogin = (core != null);
        this.courseInfo = null;
    }

    /*TODO create reset method for removing all cached data that is called
     * when working directory is changed or by user's demand, also use it in
     * constructor.
     */

    public void setApp(Application app) {
        this.application = app;
    }

    /**
     * Get singleton Application object.
     *
     * @return application object
     */
    public Application getApp() {
        if (application == null) {
            throw new RuntimeException("Application isn't set in some tests.");
        }
        return application;
    }

    //TODO get rid of this (use properties)
    @Deprecated
    public boolean inTests() {
        return inTest;
    }

    /**
     * Get singleton Io object.
     *
     * @return io object
     */
    public Io getIo() {
        return io;
    }

    public boolean hasLogin() {
        return hasLogin;
    }

    /**
     * Get singleton WorkDir object.
     *
     * @return singleton work dir object
     */
    public WorkDir getWorkDir() {
        return workDir;
    }

    /**
     * Create local course info file after download.
     *
     * @param course local copy of course object
     * @return cached course data object
     */
    public CourseInfo createCourseInfo(Course course) {
        return new CourseInfo(settings, course);
    }

    /**
     * Get map of the properties.
     *
     * @return the whole mutable map
     */
    public HashMap<String, String> getProperties() {
        // Loads properties from the global configuration file in .config/tmc-cli/
        return this.properties;
    }

    /**
     * Save the properties map into the a file.
     *
     * @return true if success
     */
    public boolean saveProperties() {
        // Saves properties to the global configuration file in .config/tmc-cli/
        return SettingsIo.saveProperties(properties);
    }

    /**
     * Lazy load the course info from course directory.
     *
     * @return local course info
     */
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
            //TODO add a way to rewrite the corrupted course config file.
            return null;
        }
        return this.courseInfo;
    }

    /**
     * Getter of TmcCore for TmcUtil class.
     * This method should never be used except in TmcUtil class.
     *
     * @return global tmcutil
     */
    public TmcCore getTmcCore() {
        if (this.tmcCore == null) {
            throw new RuntimeException("The loadBackend* method was NOT called");
        }
        return this.tmcCore;
    }

    /**
     * Initialize the tmc-core and other cached info.
     * Use this method if you need i
     * @return true if success
     */
    public boolean loadBackend() {
        if (this.tmcCore != null) {
            return true;
        }

        if (! createTmcCore()) {
            return false;
        }

        //Bug: what if we have wrong login?
        if (!hasLogin) {
            if (courseInfo == null) {
                // if user is not in course folder.
                io.println("You are not logged in. Log in using: tmc login");
            } else {
                io.println("You are not logged in as " + courseInfo.getUsername()
                        + ". Log in using: tmc login");
            }
            return false;
        }
        return true;
    }

    /**
     * Initialize the cached data, but don't fail if there is not login.
     *
     * @return true if success
     */
    public boolean loadBackendWithoutLogin() {
        if (this.tmcCore != null) {
            return true;
        }

        return createTmcCore();
    }

    /**
     * Copy login info from different settings object and them.
     * TODO: separate settings object and login info.
     * @param account login info
     */
    public void useAccount(Account account) {
        if (this.tmcCore == null) {
            createTmcCore(account);
        }
        this.settings.setAccount(account);
    }

    private void createTmcCore(Account account) {
        TaskExecutor tmcLangs;

        tmcLangs = new TaskExecutorImpl();
        this.settings.setAccount(account);
        this.tmcCore = new TmcCore(settings, tmcLangs);
        settings.setWorkDir(workDir);
    }

    private boolean createTmcCore() {
        Account cachedAccount = null;
        AccountList list = SettingsIo.loadAccountList();

        if (workDir.getConfigFile() != null) {
            // If we're in a course directory, we load settings matching the course
            // Otherwise we just load the last used settings
            courseInfo = getCourseInfo();
            if (courseInfo != null) {
                cachedAccount = list.getAccount(courseInfo.getUsername(),
                        courseInfo.getServerAddress());
            }
        } else {
            // Bug: if we are not inside course directory
            // then we may not correctly guess the correct settings.
            cachedAccount = list.getAccount();
        }

        hasLogin = (cachedAccount != null);
        createTmcCore(cachedAccount);
        return true;
    }
}
