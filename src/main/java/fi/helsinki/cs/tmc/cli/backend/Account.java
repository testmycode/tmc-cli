package fi.helsinki.cs.tmc.cli.backend;

import com.google.common.base.Optional;
import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.OauthCredentials;
import fi.helsinki.cs.tmc.core.domain.Organization;

/**
 * This object stores all login info.
 */
public class Account {

    private String serverAddress;
    private String username;
    private String password;
    private OauthCredentials oauthCredentials;
    private Course currentCourse;
    private String token;
    private Organization organization;
    private boolean sendDiagnostics;
    private static final String DEFAULT_SERVER =  "https://tmc.mooc.fi/staging";

    // for gson
    public Account() {
        this.serverAddress = DEFAULT_SERVER;
    }

    public Account(String username, String password) {
        this.serverAddress = DEFAULT_SERVER;
        this.username = username == null ? null : username.trim();
        this.password = password == null ? null : password.trim();
    }


    public Account(String username, String password, Organization organization) {
        this.serverAddress = DEFAULT_SERVER;
        this.username = username == null ? null : username.trim();
        this.password = password == null ? null : password.trim();
        this.organization = organization;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public Optional<String> getPassword() {
        return Optional.fromNullable(password);
    }

    public Optional<String> getUsername() {
        return Optional.fromNullable(username);
    }

    private static boolean stringEquals(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Account)) {
            return false;
        }
        Account another = (Account) obj;
        return stringEquals(this.password, another.password)
                && stringEquals(this.username, another.username);
    }

    public void setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
    }

    public Optional<OauthCredentials> getOauthCredentials() {
        return Optional.fromNullable(oauthCredentials);
    }

    public void setOauthCredentials(Optional<OauthCredentials> oauthCredentials) {
        this.oauthCredentials = oauthCredentials.orNull();
    }

    public void setPassword(Optional<String> password) {
        if (password.isPresent()) {
            this.password = password.get().trim();
        }
        this.password = null;
    }

    public Optional<String> getoAuthToken() {
        return Optional.of(this.token);
    }

    public void setoAuthToken(Optional<String> token) {
        this.token= token.orNull();
    }

    public Optional<Course> getCurrentCourse() {
        return Optional.fromNullable(currentCourse);
    }

    public void setCurrentCourse(Optional<Course> currentCourse) {
        this.currentCourse = currentCourse.orNull();
    }

    public Optional<Organization> getOrganization() {
        return Optional.fromNullable(this.organization);
    }

    public void setOrganization(Optional<Organization> organization) {
        this.organization = organization.orNull();
    }

    public void setSendDiagnostics(boolean value) {
        this.sendDiagnostics = value;
    }

    public boolean getSendDiagnostics() {
        return this.sendDiagnostics;
    }
}
