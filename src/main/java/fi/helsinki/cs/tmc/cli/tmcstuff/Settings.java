package fi.helsinki.cs.tmc.cli.tmcstuff;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;

import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class Settings implements TmcSettings {

    private String serverAddress;
    private String username;
    private String password;

    public Settings(String serverAddress, String username, String password) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
    }

    public Settings() {
        this.serverAddress = System.getenv("TMC_SERVER_ADDRESS");
        this.username = System.getenv("TMC_USERNAME");
        this.password = System.getenv("TMC_PASSWORD");

        if (this.serverAddress == null || this.username == null
                || this.password == null) {
            String msg = "Env variables for tmc-cli Settings are missing!"
                    + "\nTMC_SERVER_ADDRESS, TMC_USERNAME, TMC_PASSWORD";
            System.out.println(msg);
            System.exit(1);
        }
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
        //return "tmc-cli";
        return "";
    }

    @Override
    public String clientVersion() {
        return "0.1.0";
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
        return Paths.get(System.getProperty("user.dir"));
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
