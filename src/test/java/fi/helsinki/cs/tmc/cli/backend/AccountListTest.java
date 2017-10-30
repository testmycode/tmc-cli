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

    @Before
    public void setUp() {
        list = new AccountList();
        account = new Account("testuser", "testpassword");
    }

    @Test
    public void newHolderIsEmpty() {
        assertEquals(0, list.getAccountCount());
    }

    @Test
    public void addingAccountIncreasesHolderCount() {
        list.addAccount(new Account("aaa", "ooo"));
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
        list.addAccount(new Account( "2", "e"));
        list.addAccount(new Account( "-", "D"));
        assertEquals(3, list.getAccountCount());
    }

    @Test
    public void loadingLatestAccountWorks() {
        list.addAccount(new Account("-", "D"));
        list.addAccount(new Account("2", "e"));
        list.addAccount(account);
        Account latest = list.getAccount();
        assertSame(account, latest);
    }

    @Test
    public void gettingAccountByNameAndServerWorks() {
        Account wanted = new Account("2", "e");
        wanted.setServerAddress("1");
        list.addAccount(new Account("-", "D"));
        list.addAccount(wanted);
        list.addAccount(new Account("wc", "fffssshhhh aaahhh"));
        Account get = list.getAccount("2", "1");
        assertSame(wanted, get);
    }

    @Test
    public void gettingLatestAccountSetsItToTheTop() {
        Account wanted = new Account("2", "e");
        wanted.setServerAddress("1");
        list.addAccount(wanted);
        list.addAccount(new Account("-", "D"));
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
        Account account2 = new Account("2", "e");
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
