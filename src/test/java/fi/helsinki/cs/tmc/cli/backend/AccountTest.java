package fi.helsinki.cs.tmc.cli.backend;

import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.core.domain.Organization;
import org.junit.Before;
import org.junit.Test;

public class AccountTest {

    private Account account;
    private Organization testOrganization;

    @Before
    public void setUp() {
        testOrganization = new Organization("test", "test", "hy", "test", false);
        account = new Account("testuser", "testpassword", testOrganization);
    }

    @Test
    public void equalsWorksWithSameValues() {
        Account compared = new Account("testuser", "testpassword", testOrganization);
        assertEquals(true, account.equals(compared));
    }

    @Test
    public void equalsWorksWithNull() {
        Account compared = new Account("testuser", null, testOrganization);
        assertEquals(false, account.equals(compared));
    }

    @Test
    public void equalsWorksWithRandomValue() {
        Account compared = new Account( "asd", "testpassword", testOrganization);
        assertEquals(false, account.equals(compared));
    }
}
