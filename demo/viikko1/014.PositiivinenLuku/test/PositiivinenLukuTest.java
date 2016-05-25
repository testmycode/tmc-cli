
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Rule;
import org.junit.Test;

@Points("14")
public class PositiivinenLukuTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void test() {
        testPositiivinenLuku(1);
    }

    @Test
    public void testToinen() {
        testPositiivinenLuku(0);
    }

    @Test
    public void testKolmas() {
        testPositiivinenLuku(-1);
    }

    private void testPositiivinenLuku(int luku) {
        ReflectionUtils.newInstanceOfClass(PositiivinenLuku.class);
        io.setSysIn(luku + "\n");
        PositiivinenLuku.main(new String[0]);

        String out = io.getSysOut();

        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);

        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        if (luku > 0) {
            assertTrue("Tulosteessasi pitäisi olla teksti \"on positiivinen\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("on positiivinen"));
            assertFalse("Tulosteessasi ei saa olla tekstiä \"ei ole positiivinen\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("ei ole positiivinen"));
        } else {
            assertTrue("Tulosteessasi pitäisi olla teksti \"ei ole positiivinen\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("ei ole positiivinen"));
            assertFalse("Tulosteessasi ei saa olla tekstiä \"on positiivinen\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("on positiivinen"));
        }
    }
}
