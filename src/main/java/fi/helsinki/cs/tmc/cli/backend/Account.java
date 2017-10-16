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

    static Account NULL_ACCOUNT = new Account(null, null, null);

    private String serverAddress;
    private String username;
    private String password;
    private OauthCredentials oauthCredentials;
    private Course currentCourse;
    private String token;
    private Organization organization;

    // for gson
    public Account() {}

    public Account(String serverAddress, String username, String password) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
        this.organization = new Organization("Default", "lolled", "default", "lolled", false);
    }

    public Optional<String> getServerAddress() {
        return Optional.fromNullable(serverAddress);
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
        return stringEquals(this.serverAddress, another.serverAddress)
                && stringEquals(this.username, another.username);
    }

    public void setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
    }

    public Optional<OauthCredentials> getOauthCredentials() {
        return Optional.fromNullable(oauthCredentials);
    }

    public void setOauthCredentials(Optional<OauthCredentials> oauthCredentials) {
        if (oauthCredentials.isPresent()) {
            this.oauthCredentials = oauthCredentials.get();
        } else {
            this.oauthCredentials = null;
        }
    }

    public void setPassword(Optional<String> password) {
        if (password.isPresent()) {
            this.password = password.get();
        } else {
            this.password = null;
        }
    }

    public Optional<String> getoAuthToken() {
        return Optional.of(this.token);
    }

    public void setoAuthToken(Optional<String> token) {
        if (token.isPresent()) {
            this.token = token.get();
        } else {
            this.token = null;
        }
    }

    public Optional<Course> getCurrentCourse() {
        return Optional.fromNullable(currentCourse);
    }

    @Override
    public String toString() {
        return "Account{" +
                "serverAddress='" + serverAddress + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", oauthCredentials='" + oauthCredentials + '\'' +
                ", token='" + token + '\'' +
                ", currentCourse='" + currentCourse +  '\'' +
                '}';
    }

    public void setCurrentCourse(Optional<Course> currentCourse) {
        if (currentCourse.isPresent()) {
            this.currentCourse = currentCourse.get();
        } else {
            this.currentCourse = null;
        }

    }

    public Optional<Organization> getOrganization() {
        return Optional.fromNullable(this.organization);
    }

    public void setOrganization(Optional<Organization> organization) {
        if (organization.isPresent()) {
            this.organization = organization.get();
        } else {
            this.organization = null;
        }
    }
}
