
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;

@Points("10")
public class YmpyranKehanPituusTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testYmpyranKehanPituus() {
        testYmpyranKehanPituus(3);
    }

    private void testYmpyranKehanPituus(int sade) {
        ReflectionUtils.newInstanceOfClass(YmpyranKehanPituus.class);
        io.setSysIn(sade + "\n");
        YmpyranKehanPituus.main(new String[0]);

        String out = io.getSysOut();

        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);

        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Tulosteessasi pitäisi olla teksti \"Ympyrän kehä:\", nyt ei ollut. Tulosteesi oli: " + out,
                out.contains("mpyrän kehä:"));

        String kehaMjono = out.substring(out.indexOf("mpyrän kehä:") + "mpyrän kehä:".length());
        double keha;
        try {
            keha = Double.parseDouble(kehaMjono.trim());
        } catch (Exception e) {
            fail("Kirjoita ympyrän kehä tulokseen. Esim: \"Ympyrän kehä: 125.6634\". Tulostit nyt: \"" + kehaMjono + "\".");
            return;
        }

        double oikeatulos = (Math.PI * 2 * sade);
        assertEquals("Ympyrän kehän pituus säteellä " + sade +
                " pitäisi olla " + oikeatulos + ", ehdotit: " +
                kehaMjono.trim(), oikeatulos, keha, 0.001);
    }
}
