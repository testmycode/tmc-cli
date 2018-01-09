package fi.helsinki.cs.tmc.cli.core;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
import fi.helsinki.cs.tmc.cli.backend.CourseInfo;
import fi.helsinki.cs.tmc.cli.backend.CourseInfoIo;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.io.*;
import fi.helsinki.cs.tmc.cli.shared.CourseFinder;
import fi.helsinki.cs.tmc.core.TmcCore;
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
    private AnalyticsFacade analyticsFacade;

    public CliContext(Io io, TmcCore core, WorkDir workDir, Settings settings, AnalyticsFacade facade) {
        inTest = (io != null);
        if (!inTest) {
            io = new TerminalIo(System.in);
        }
        this.io = io;
        this.workDir = workDir;
        this.properties = SettingsIo.loadProperties();
        this.settings = settings;
        this.tmcCore = core;
        this.hasLogin = (core != null);
        this.courseInfo = null;
        this.analyticsFacade = facade;
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
     * Create new instance of course finder.
     * This is used so that it's easy to mock out the course finder.
     *
     * @return new instance of CourseFinder
     */
    public CourseFinder createCourseFinder() {
        return new CourseFinder(this);
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
            io.errorln(
                    "Course configuration file "
                            + workDir.getConfigFile().toString()
                            + "is invalid.");
            //TODO add a way to rewrite the corrupted course config file.
            return null;
        }
        return courseInfo;
    }

    /**
     * Getter of TmcCore for TmcUtil class.
     * This method should never be used except in TmcUtil class.
     *
     * @return global tmcutil
     */
    public TmcCore getTmcCore() {
        return this.tmcCore;
    }

    /**
     * Initialize the tmc-core and other cached info.
     * Use this method if you need i
     * @return true if success
     * @param quiet
     */
    public boolean checkIsLoggedIn(boolean quiet) {
        loadUserInformation();
        //Bug: what if we have wrong login?
        if (!hasLogin) {
            if (quiet) {
                return false;
            }
            if (courseInfo == null) {
                // if user is not in course folder.
                io.errorln("You are not logged in. Log in using: tmc login");
            } else {
                io.errorln(
                        "You are not logged in as "
                                + courseInfo.getUsername()
                                + ". Log in using: tmc login");
            }
            return false;
        }
        return true;
    }

    public Settings getSettings() {
        return settings;
    }

    /**
     * Copy login info from different settings object.
     * @param account login info
     */
    public void useAccount(Account account) {
        this.settings.setAccount(account);
    }

    public AnalyticsFacade getAnalyticsFacade() {
        return this.analyticsFacade;
    }

    public void loadUserInformation() {
        Account cachedAccount = null;
        AccountList list = SettingsIo.loadAccountList();

        if (list == null) {
            hasLogin = false;
            return;
        }

        if (workDir.getConfigFile() != null) {
            // If we're in a course directory, we load settings matching the course
            // Otherwise we just load the last used settings
            courseInfo = getCourseInfo();
            if (courseInfo != null) {
                cachedAccount =
                        list.getAccount(courseInfo.getUsername(), courseInfo.getServerAddress());
            }
        } else {
            // Bug: if we are not inside course directory
            // then we may not correctly guess the correct settings.
            cachedAccount = list.getAccount();
        }
        hasLogin = cachedAccount != null;
        this.settings.setAccount(cachedAccount);
        settings.setWorkDir(workDir);
    }

    //TODO rename this as getColorProperty and move it somewhere else
    public Color getColorProperty(String propertyName, Application application) {
        String propertyValue = getProperties().get(propertyName);
        Color color = ColorUtil.getColor(propertyValue);
        if (color == null) {
            switch (propertyName) {
                case "progressbar-left":
                    return Color.CYAN;
                case "progressbar-right":
                    return Color.CYAN;
                case "testresults-left":
                    return Color.GREEN;
                case "testresults-right":
                    return Color.RED;
                default:
                    return Color.NONE;
            }
        }
        return color;
    }
}
