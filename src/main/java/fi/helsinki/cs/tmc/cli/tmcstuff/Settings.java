package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.nio.file.Path;
import java.util.Locale;

public class Settings implements TmcSettings {

    private String serverAddress;
    private String username;
    private String password;

    private transient WorkDir workDir;

    public Settings(String serverAddress, String username, String password) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
    }
    
    public Settings() {
    }

    /**
     * This method is used for changing the main settings object.
     * @param settings settings object where from the new values are copied
     */
    public void set(Settings settings) {
        this.serverAddress = settings.serverAddress;
        this.username = settings.username;
        this.password = settings.password;
    }

    public void setWorkDir(WorkDir workDir) {
        this.workDir = workDir;
    }

    @Override
    public String getServerAddress() {
        return serverAddress;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean userDataExists() {
        return this.username != null && this.password != null
                && !this.username.isEmpty() && !this.password.isEmpty();
    }

    @Override
    public Optional<Course> getCurrentCourse() {
        return Optional.absent();
    }

    @Override
    public String apiVersion() {
        return "7";
    }

    @Override
    public String clientName() {
        return "tmc_cli";
    }

    @Override
    public String clientVersion() {
        return EnvironmentUtil.getVersion();
    }

    @Override
    public String getFormattedUserData() {
        if (!userDataExists()) {
            return "";
        }
        return this.username + ":" + this.password;
    }

    @Override
    public Path getTmcProjectDirectory() {
        return workDir.getTmcDirectory();
    }

    @Override
    public Locale getLocale() {
        return new Locale("EN");
    }

    @Override
    public SystemDefaultRoutePlanner proxy() {
        return null;
    }

    @Override
    public Path getConfigRoot() {
        return SettingsIo.getDefaultConfigRoot();
    }

    @Override
    public void setCourse(Course course) {
    }

    @Override
    public void setConfigRoot(Path path) {
    }
}
