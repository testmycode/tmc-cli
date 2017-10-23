package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import fi.helsinki.cs.tmc.core.domain.Organization;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class AccountListTest {

    private AccountList list;
    private Account account;
    private static final Organization ORGANIZATION = new Organization("test", "test", "hy", "test", false);

    @Before
    public void setUp() {
        list = new AccountList();
        account = new Account("testserver", "testuser", "testpassword", ORGANIZATION);
    }

    @Test
    public void newHolderIsEmpty() {
        assertEquals(0, list.getAccountCount());
    }

    @Test
    public void addingAccountIncreasesHolderCount() {
        list.addAccount(new Account("eee", "aaa", "ooo", ORGANIZATION));
        assertEquals(1, list.getAccountCount());
    }

    @Test
    public void loadingFromHolderWorks() {
        list.addAccount(account);
        Account loaded = list.getAccount();
        assertSame(account, loaded);
    }

    @Test
    public void addingMoreThanOneSettingWorks() {
        list.addAccount(account);
        list.addAccount(new Account("1", "2", "e", ORGANIZATION));
        list.addAccount(new Account(":", "-", "D", ORGANIZATION));
        assertEquals(3, list.getAccountCount());
    }

    @Test
    public void loadingLatestAccountWorks() {
        list.addAccount(new Account(":", "-", "D", ORGANIZATION));
        list.addAccount(new Account("1", "2", "e", ORGANIZATION));
        list.addAccount(account);
        Account latest = list.getAccount();
        assertSame(account, latest);
    }

    @Test
    public void gettingAccountByNameAndServerWorks() {
        Account wanted = new Account("1", "2", "e", ORGANIZATION);
        list.addAccount(new Account(":", "-", "D", ORGANIZATION));
        list.addAccount(wanted);
        list.addAccount(new Account("344", "wc", "fffssshhhh aaahhh", ORGANIZATION));
        Account get = list.getAccount("2", "1");
        assertSame(wanted, get);
    }

    @Test
    public void gettingLatestAccountSetsItToTheTop() {
        Account wanted = new Account("1", "2", "e", ORGANIZATION);
        list.addAccount(wanted);
        list.addAccount(new Account(":", "-", "D", ORGANIZATION));
        list.getAccount("2", "1");
        Account get = list.getAccount();
        assertSame(wanted, get);
    }

    @Test
    public void iterateOverZeroAccounts() {
        Iterator<Account> iterator = list.iterator();
        assertEquals(false, iterator.hasNext());
    }

    @Test
    public void iterateOverOneAccount() {
        list.addAccount(account);
        Iterator<Account> iterator = list.iterator();
        assertEquals(true, iterator.hasNext());
        assertEquals(account, iterator.next());
        assertEquals(false, iterator.hasNext());
    }

    @Test
    public void iterateOverTwoAccounts() {
        Account account2 = new Account("1", "2", "e", ORGANIZATION);
        list.addAccount(account);
        list.addAccount(account2);
        Iterator<Account> iterator = list.iterator();
        assertEquals(true, iterator.hasNext());
        assertEquals(account2, iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(account, iterator.next());
        assertEquals(false, iterator.hasNext());
    }
}
