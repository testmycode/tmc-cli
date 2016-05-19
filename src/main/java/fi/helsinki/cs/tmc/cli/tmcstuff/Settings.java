package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;

import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class Settings implements TmcSettings {

    private String serverAddress;
    private String password;
    private String username;

    public Settings(String serverAddress, String password, String username) {
        this.serverAddress = serverAddress;
        this.password = password;
        this.username = username;
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
        return false;
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
        return "tmc-cli";
    }

    @Override
    public String clientVersion() {
        return "0.1.0";
    }

    @Override
    public String getFormattedUserData() {
        return null;
    }

    @Override
    public Path getTmcProjectDirectory() {
        return Paths.get("/tmp/tmc-cli");
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
        return getTmcProjectDirectory();
    }

    @Override
    public void setCourse(Course course) {
    }

    @Override
    public void setConfigRoot(Path path) {
    }

}
