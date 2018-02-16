package fi.helsinki.cs.tmc.cli.backend;

import java.util.*;

/**
 * This is a class for storing all different accounts as a single array.
 */
public class AccountList implements Iterable<Account> {

    private List<Account> accountArray;

    public AccountList() {
        this.accountArray = new ArrayList<>();
    }

    public Account getAccount() {
        if (this.accountArray.size() > 0) {
            // Get last used account by default
            return this.accountArray.get(0);
        }
        return null;
    }

    public Account getAccount(String username) {
        if (username == null) {
            return getAccount();
        }
        for (Account account : this.accountArray) {
            if (!account.getUsername().isPresent()) {
                continue;
            }
            if (account.getUsername().get().equals(username)) {
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
            if (account.getUsername().equals(newSettings.getUsername())) {
                // Replace old account if username and server match
                this.accountArray.remove(account);
                break;
            }
        }
        this.accountArray.add(0, newSettings);
    }

    public void deleteAccount(String username) {
        Set<Account> removables = new HashSet<>();
        for (Account account : this.accountArray) {
            if (!account.getUsername().isPresent()) {
                removables.add(account);
            } else if (account.getUsername().get().equals(username)) {
                removables.add(account);
                break;
            }
        }
        if (!removables.isEmpty()) {
            this.accountArray.removeAll(removables);
        }
    }

    public void deleteAllAccounts() {
        this.accountArray = new ArrayList<>();
    }

    public int getAccountCount() {
        return this.accountArray.size();
    }

    private List<Account> getAccountList() {
        return Collections.unmodifiableList(this.accountArray);
    }

    @Override
    public Iterator<Account> iterator() {
        return getAccountList().iterator();
    }
}
