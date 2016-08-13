package fi.helsinki.cs.tmc.cli.backend;

/**
 * This object stores all login info.
 */
public class Account {

    static Account NULL_ACCOUNT = new Account(null, null, null);

    private String serverAddress;
    private String username;
    private String password;

    // for gson
    public Account() {}

    public Account(String serverAddress, String username, String password) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
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
                && stringEquals(this.username, another.username)
                && stringEquals(this.password, another.password);
    }
}
