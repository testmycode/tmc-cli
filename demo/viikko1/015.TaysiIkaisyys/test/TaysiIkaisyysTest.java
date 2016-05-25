
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;

@Points("15")
public class TaysiIkaisyysTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void test() {
        testTaysiIkaisyys(17);
    }

    @Test
    public void testToinen() {
        testTaysiIkaisyys(18);
    }

    @Test
    public void testKolmas() {
        testTaysiIkaisyys(19);
    }

    private void testTaysiIkaisyys(int luku) {
        ReflectionUtils.newInstanceOfClass(TaysiIkaisyys.class);
        io.setSysIn(luku + "\n");
        TaysiIkaisyys.main(new String[0]);

        String out = io.getSysOut();

        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);

        assertTrue("Kysymyksessäsi pitäisi olla merkki ?, nyt ei ollut. Kysyit: " + out,
                out.contains("?"));

        String viesti = "Kun ikä on "+luku+", ";
        if (luku >= 18) {
            assertTrue(viesti+"tulosteessasi pitäisi olla teksti \"Olet jo täysi-ikäinen\", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("let jo t"));
            assertTrue(viesti+"tulosteessasi ei pitäisi olla tekstiä \"Et ole vielä täysi-ikäinen\", nyt oli. Tulosteesi oli: " + out,
                    !out.contains("t ole viel"));
        } else {
            assertTrue("Tulosteessasi pitäisi olla teksti \"Et ole vielä täysi-ikäinen\", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("t ole viel"));
            assertTrue(viesti+"tulosteessasi ei pitäisi olla tekstiä \"Olet jo täysi-ikäinen\", nyt oli. Tulosteesi oli: " + out,
                    !out.contains("let jo t"));
        }
    }
}
