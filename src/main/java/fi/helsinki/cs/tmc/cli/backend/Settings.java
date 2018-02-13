package fi.helsinki.cs.tmc.cli.backend;

import fi.helsinki.cs.tmc.cli.command.LoginCommand;
import fi.helsinki.cs.tmc.cli.command.OrganizationCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.WorkDir;

import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.core.domain.OauthCredentials;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.utilities.TmcServerAddressNormalizer;
import fi.helsinki.cs.tmc.spyware.SpywareSettings;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;

public class Settings implements TmcSettings, SpywareSettings {

    private static final Logger logger = LoggerFactory.getLogger(Settings.class);
    private WorkDir workDir;
    private Account account;

    public Settings() {
        this.account = new Account();
    }

    public Settings(String username, String password, Organization organization) {
        this.account = new Account(username, password, organization);
    }

    /**
     * This method is used for changing the main settings object.
     * @param account account that has the login info
     */
    public void setAccount(CliContext context, Account account) {
        if (account == null) {
            account = new Account();
        }
        Account prevAccount = this.account;
        this.account = account;
        try {
            Optional<String> password = account.getPassword();
            if(password.isPresent()) {
                if (!migrateAccount(context, account)) {
                    return;
                }
                SettingsIo.saveCurrentSettingsToAccountList(this);
            }
        } catch (Exception e) {
            logger.warn("Settings migration failed.");
            this.account = prevAccount;
        }
    }

    private boolean migrateAccount(CliContext context, Account account) throws Exception {
        Io io = context.getIo();
        // the old server doesn't have any organizations
        if (account.getServerAddress().contains("tmc.mooc.fi/mooc")) {
            return false;
        }
        TmcServerAddressNormalizer normalizer = new TmcServerAddressNormalizer();
        normalizer.normalize();
        normalizer.selectOrganizationAndCourse();
        io.println("TMC cli has been updated. Please provide the following information.");
        if (!context.getSettings().getOrganization().isPresent()) {
            OrganizationCommand organizationCommand = new OrganizationCommand();
            Optional<Organization> organization = organizationCommand.chooseOrganization(context, null);
            if (!organization.isPresent()) {
                return false;
            }
            account.setOrganization(organization);
        }
        Callable<Void> callable = context.getTmcCore().authenticate(ProgressObserver.NULL_OBSERVER, account.getPassword().get());
        callable.call();
        LoginCommand loginCommand = new LoginCommand();
        boolean sendDiagnostics = loginCommand.getAnswerFromUser(null, account.getServerAddress(),
                "Do you want to send crash reports for client development?",
                false,
                io);
        account.setSendDiagnostics(sendDiagnostics);
        boolean sendAnalytics = loginCommand.getAnswerFromUser(null, account.getServerAddress(),
                "Do you want to send analytics data for research?",
                false,
                io);
        account.setSendAnalytics(sendAnalytics);
        io.println("");
        account.setPassword(Optional.absent());
        return true;
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
    public void setServerAddress(String address) {
       account.setServerAddress(address);
    }

    @Override
    public Optional<String> getPassword() {
        return account.getPassword();
    }

    @Override
    public void setPassword(Optional<String> password) {
            throw new RuntimeException("Using password is deprecated");
    }

    @Override
    public Optional<String> getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean userDataExists() {
        return getUsername().isPresent() && getPassword().isPresent();
    }

    @Override
    public Optional<Course> getCurrentCourse() {
        return account.getCurrentCourse();
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
    public void setCourse(Optional<Course> course) {
        this.account.setCurrentCourse(course);
    }

    @Override
    public Path getConfigRoot() {
        return SettingsIo.getConfigDirectory();
    }

    @Override
    public String hostProgramName() {
        // which command line is used
        return "unknown";
    }

    @Override
    public String hostProgramVersion() {
        return "unknown";
    }

    @Override
    public boolean getSendDiagnostics() {
        return account.getSendDiagnostics();
    }

    public void setSendDiagnostics(boolean value) {
        account.setSendDiagnostics(value);
    }

    @Override
    public Optional<OauthCredentials> getOauthCredentials() {
        return account.getOauthCredentials();
    }

    @Override
    public void setOauthCredentials(Optional<OauthCredentials> credentials) {
        account.setOauthCredentials(credentials);
    }

    @Override
    public void setToken(Optional<String> token) {
        account.setOauthToken(token);
    }

    @Override
    public Optional<String> getToken() {
        return account.getOauthToken();
    }

    @Override
    public Optional<Organization> getOrganization() {
        return account.getOrganization();
    }

    @Override
    public void setOrganization(Optional<Organization> organization) {
        account.setOrganization(organization);
    }

    @Override
    public boolean isSpywareEnabled() {
        return account.getSendAnalytics();
    }

    public void setSpywareEnabled(boolean spywareEnabled) {
        account.setSendAnalytics(spywareEnabled);
    }

    @Override
    public boolean isDetailedSpywareEnabled() {
        return account.getSendDetailedAnalytics();
    }

    public void setDetailedSpywareEnabled(boolean detailedSpywareEnabled) {
        account.setSendDetailedAnalytics(detailedSpywareEnabled);
    }
}
