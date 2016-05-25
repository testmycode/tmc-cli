
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;

@Points("11")
public class SuurempiLukuTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testLuvut() {
        testSuurempiLuku(3, 7);
    }

    @Test
    public void testLuvutNegatiiviset() {
        testSuurempiLuku(-5, -3);
    }

    @Test
    public void testLuvutSamat() {
        testSuurempiLuku(3, 3);
    }

    private void testSuurempiLuku(int eka, int toka) {
        ReflectionUtils.newInstanceOfClass(SuurempiLuku.class);
        io.setSysIn(eka + "\n" + toka + "\n");
        SuurempiLuku.main(new String[0]);

        String out = io.getSysOut();

        assertTrue("Et tulostanut mitään! Sinun pitäisi tulostaa kysymys!", out.trim().length() > 0);

        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Tulosteessasi pitäisi olla teksti \"Luvuista suurempi:\", nyt ei ollut. Tulosteesi oli: " + out,
                out.contains("uvuista suurempi:"));

        String mjono = out.substring(out.indexOf("uvuista suurempi:") + "uvuista suurempi:".length());
        double suurempi = 0.0;
        try {
            suurempi = Double.parseDouble(mjono.trim());
        } catch (Exception e) {
            fail("Kirjoita suurempi luku vastaukseen. Esim: \"Luvuista suurempi: 42\"");
        }

        int oikea = Math.max(eka, toka);
        assertEquals("Luvuista " + eka + " ja " + toka + " suuremman pitäisi olla " + oikea + ", ehdotit: " + mjono.trim(), oikea, suurempi, 0.001);
    }
}
