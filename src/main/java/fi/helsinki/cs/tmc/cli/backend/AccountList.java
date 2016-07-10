package fi.helsinki.cs.tmc.cli.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This is a class for storing all different accounts as a single array.
 */
public class AccountList implements Iterable<Account> {

    private List<Account> accountArray;

    public AccountList() {
        this.accountArray = new ArrayList<>();
    }

    //TODO This should not be used
    public Account getAccount() {
        if (this.accountArray.size() > 0) {
            // Get last used account by default
            return this.accountArray.get(0);
        }
        return null;
    }

    public Account getAccount(String username, String server) {
        if (username == null || server == null) {
            return getAccount();
        }
        for (Account account : this.accountArray) {
            if (account.getUsername().equals(username)
                    && account.getServerAddress().equals(server)) {
                // Move account to index 0 so we can always use the last used account by default
                this.accountArray.remove(account);
                this.accountArray.add(0, account);
                return account;
            }
        }
        return null;
    }

    public void addAccount(Account newSettings) {
        for (Account account : this.accountArray) {
            if (account.getUsername().equals(newSettings.getUsername())
                    && account.getServerAddress().equals(newSettings.getServerAddress())) {
                // Replace old account if username and server match
                this.accountArray.remove(account);
                break;
            }
        }
        this.accountArray.add(0, newSettings);
    }

    public void deleteAccount(String username, String server) {
        Account remove = null;
        for (Account account : this.accountArray) {
            if (account.getUsername().equals(username)
                    && account.getServerAddress().equals(server)) {
                remove = account;
                break;
            }
        }
        if (remove != null) {
            this.accountArray.remove(remove);
        }
    }

    public void deleteAllAccounts() {
        this.accountArray = new ArrayList<>();
    }

    public int getAccountCount() {
        return this.accountArray.size();
    }

    public List<Account> getAccountList() {
        return Collections.unmodifiableList(this.accountArray);
    }

    @Override
    public Iterator<Account> iterator() {
        return getAccountList().iterator();
    }
}
