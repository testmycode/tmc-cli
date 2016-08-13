package fi.helsinki.cs.tmc.cli.backend;

import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.nio.file.Path;
import java.util.Locale;

public class Settings implements TmcSettings {

    private WorkDir workDir;
    private Account account;

    public Settings() {
        this.account = Account.NULL_ACCOUNT;
    }

    public Settings(String serverAddress, String username, String password) {
        this.account = new Account(serverAddress, username, password);
    }

    /**
     * This method is used for changing the main settings object.
     * @param account account that has the login info
     */
    public void setAccount(Account account) {
        if (account == null) {
            /* NULL_ACCOUNT is used so that the NullPointerException
             * is not thrown in the getters.
             */
            account = Account.NULL_ACCOUNT;
        }
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public void setWorkDir(WorkDir workDir) {
        this.workDir = workDir;
    }

    @Override
    public String getServerAddress() {
        return account.getServerAddress();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean userDataExists() {
        return getUsername() != null && getPassword() != null;
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
        return getUsername() + ":" + this.getPassword();
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
        return SettingsIo.getConfigDirectory();
    }

    @Override
    public void setCourse(Course course) {}

    @Override
    public void setConfigRoot(Path path) {}
}
