package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    private AccountList accountList;
    private Account account;
    private Path tempDir;

    @Before
    public void setUp() {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve(SettingsIo.CONFIG_DIR);
        account = new Account("testserver", "testuser", "testpassword");
        accountList = new AccountList();
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) {
        }
    }

    @After
    public void cleanUp() {
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) {
        }
    }

    @Test
    public void correctConfigPath() {
        Path path = SettingsIo.getConfigDirectory();
        String fs = System.getProperty("file.separator");
        assertTrue(path.toString().contains(SettingsIo.CONFIG_DIR));
        assertTrue(path.toString().contains(fs));
        assertTrue(!path.toString().contains(fs + fs));
        assertTrue(path.toString().contains(System.getProperty("user.home")));
    }

    @Test
    public void saveZeroAccountsToFile() {
        boolean success = SettingsIo.saveAccountList(accountList, tempDir);
        assertTrue(success);
        Path path = tempDir.resolve(SettingsIo.ACCOUNTS_CONFIG);
        assertTrue(Files.exists(path));
    }

    @Test
    public void saveOneAccountToFile() {
        accountList.addAccount(account);
        boolean success = SettingsIo.saveAccountList(accountList, tempDir);
        assertTrue(success);
        Path path = tempDir.resolve(SettingsIo.ACCOUNTS_CONFIG);
        assertTrue(Files.exists(path));
    }

    @Test
    public void saveAndLoadZeroAccountsFromFile() {
        boolean success = SettingsIo.saveAccountList(accountList, tempDir);
        assertTrue(success);
        AccountList loadedList = SettingsIo.loadAccountList(tempDir);
        assertNotNull(loadedList);
        assertEquals(0, loadedList.getAccountCount());
    }

    @Test
    public void saveAndLoadOneAccountFromFile() {
        accountList.addAccount(account);
        boolean success = SettingsIo.saveAccountList(accountList, tempDir);
        assertTrue(success);
        AccountList loadedList = SettingsIo.loadAccountList(tempDir);
        assertNotNull(loadedList);
        assertEquals(1, loadedList.getAccountCount());
        Account loadedAccount = loadedList.iterator().next();
        assertEquals(account.getUsername(), loadedAccount.getUsername());
        assertEquals(account.getPassword(), loadedAccount.getPassword());
        assertEquals(account.getServerAddress(), loadedAccount.getServerAddress());
    }

    @Test
    public void loadingWhenNoFilePresentReturnsNull() {
        Path path = tempDir.getParent();
        AccountList loadedList = SettingsIo.loadAccountList(path);
        assertNotNull(loadedList);
        assertEquals(0, loadedList.getAccountCount());
    }

    @Test
    public void savingPropertiesWorks() {
        HashMap<String, String> props = new HashMap<>();
        props.put("lastupdated", "1970-01-01");
        props.put("nextupdate", "2038-01-20");
        SettingsIo.savePropertiesTo(props, tempDir);
        assertTrue(
                "Properties file exists",
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
