package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AccountTest {

    private Account account;

    @Before
    public void setUp() {
        account = new Account("testserver", "testuser", "testpassword");
    }

    @Test
    public void equalsWorksWithSameValues() {
        Account compared = new Account("testserver", "testuser", "testpassword");
        assertEquals(true, account.equals(compared));
    }

    @Test
    public void equalsWorksWithNull() {
        Account compared = new Account(null, "testuser", "testpassword");
        assertEquals(false, account.equals(compared));
    }

    @Test
    public void equalsWorksWithRandomValue() {
        Account compared = new Account("xyz", "testuser", "testpassword");
        assertEquals(false, account.equals(compared));
    }
}
