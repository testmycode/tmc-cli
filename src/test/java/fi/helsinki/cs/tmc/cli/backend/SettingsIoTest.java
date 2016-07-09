package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class SettingsIoTest {

    private Account account;
    private Path tempDir;

    @Before
    public void setUp() {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve(SettingsIo.CONFIG_DIR);
        account = new Account("testserver", "testuser", "testpassword");
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) { }
    }

    @After
    public void cleanUp() {
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) { }
    }

    @Test
    public void correctConfigPath() {
        Path path = SettingsIo.getDefaultConfigRoot();
        String fs = System.getProperty("file.separator");
        assertTrue(path.toString().contains(SettingsIo.CONFIG_DIR));
        assertTrue(path.toString().contains(fs));
        assertTrue(!path.toString().contains(fs + fs));
        assertTrue(path.toString().contains(System.getProperty("user.home")));
    }

    @Test
    public void savingToFileWorks() {
        Boolean success = SettingsIo.saveTo(account, tempDir);
        assertTrue(success);
        Path path = tempDir
                .resolve(SettingsIo.ACCOUNTS_CONFIG);
        assertTrue(Files.exists(path));
    }

    @Test
    public void loadingFromFileWorks() {
        SettingsIo.saveTo(account, tempDir);
        Account loadedAccount;
        loadedAccount = SettingsIo.loadFrom(tempDir);
        assertNotNull(loadedAccount);
        assertEquals(account.getUsername(), loadedAccount.getUsername());
        assertEquals(account.getPassword(), loadedAccount.getPassword());
        assertEquals(account.getServerAddress(), loadedAccount.getServerAddress());
    }

    @Test
    public void loadingWhenNoFilePresentReturnsNull() {
        Path path = tempDir.getParent();
        Account loadedAccount = SettingsIo.loadFrom(path);
        assertEquals(null, loadedAccount);
    }

    @Test
    public void newHolderIsEmpty() {
        AccountList holder = new AccountList();
        assertEquals(0, holder.accountCount());
    }

    @Test
    public void addingAccountIncreasesHolderCount() {
        AccountList holder = new AccountList();
        holder.addAccount(new Account("eee", "aaa", "ooo"));
        assertEquals(1, holder.accountCount());
    }

    @Test
    public void loadingFromHolderWorks() {
        AccountList holder = new AccountList();
        holder.addAccount(account);
        Account loaded = holder.getAccount();
        assertSame(account, loaded);
    }

    @Test
    public void addingMoreThanOneSettingWorks() {
        AccountList holder = new AccountList();
        holder.addAccount(account);
        holder.addAccount(new Account("1", "2", "e"));
        holder.addAccount(new Account(":", "-", "D"));
        assertEquals(3, holder.accountCount());
    }

    @Test
    public void loadingLatestAccountWorks() {
        AccountList holder = new AccountList();
        holder.addAccount(new Account(":", "-", "D"));
        holder.addAccount(new Account("1", "2", "e"));
        holder.addAccount(account);
        Account latest = holder.getAccount();
        assertSame(account, latest);
    }

    @Test
    public void gettingAccountByNameAndServerWorks() {
        AccountList holder = new AccountList();
        Account wanted = new Account("1", "2", "e");
        holder.addAccount(new Account(":", "-", "D"));
        holder.addAccount(wanted);
        holder.addAccount(new Account("344", "wc", "fffssshhhh aaahhh"));
        Account get = holder.getAccount("2", "1");
        assertSame(wanted, get);
    }

    @Test
    public void gettingLatestAccountSetsItToTheTop() {
        AccountList holder = new AccountList();
        Account wanted = new Account("1", "2", "e");
        holder.addAccount(wanted);
        holder.addAccount(new Account(":", "-", "D"));
        holder.getAccount("2", "1");
        Account get = holder.getAccount();
        assertSame(wanted, get);
    }

    @Test
    public void savingPropertiesWorks() {
        HashMap<String, String> props = new HashMap<>();
        props.put("lastupdated", "1970-01-01");
        props.put("nextupdate", "2038-01-20");
        SettingsIo.savePropertiesTo(props, tempDir);
        assertTrue("Properties file exists",
                Files.exists(tempDir.resolve(SettingsIo.PROPERTIES_CONFIG)));
    }

    @Test
    public void loadingPropertiesWorks() {
        HashMap<String, String> props = new HashMap<>();
        props.put("lastupdated", "1970-01-01");
        props.put("nextupdate", "2038-01-20");
        SettingsIo.savePropertiesTo(props, tempDir);
        HashMap<String, String> loadedProps = SettingsIo.loadPropertiesFrom(tempDir);
        assertEquals(props, loadedProps);
    }
}
