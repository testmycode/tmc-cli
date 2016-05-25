import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.util.NoSuchElementException;
import org.junit.*;
import static org.junit.Assert.*;

public class SalasanaTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Before
    public void init() {
        ReflectionUtils.newInstanceOfClass(Salasana.class);
    }

    @Test
    @Points("22.1")
    public void ohjelmaKysyyJaVastaaKunVaarin() {
        io.setSysIn("saapas\n");
        try {
            Salasana.main(new String[0]);
        } catch (NoSuchElementException e) {
            // ei välitetä tästä, myöhemmässä vaiheessa toteutettavat
            // ohjelmat haluavat useampia syötteitä
        }


        String out = io.getSysOut();
        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);
        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Kysymyksessäsi pitäisi olla teksti salasana, nyt ei ollut. Kysyit: " + out,
                out.contains("salasana"));

        assertTrue("Syötettäessä väärä salasana tulosteessasi pitäisi olla teksti \"Väärin!\", nyt ei ollut. Tulosteesi oli: " + out,
                out.contains("äärin"));

    }

    @Test
    @Points("22.1")
    public void ohjelmaKysyyJaVastaaKunOikein() {
        io.setSysIn("porkkana\n");
        try {
            Salasana.main(new String[0]);
        } catch (NoSuchElementException e) {
            // ei välitetä tästä, myöhemmässä vaiheessa toteutettavat
            // ohjelmat haluavat useampia syötteitä
        }

        String out = io.getSysOut();
        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);
        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Kysymyksessäsi pitäisi olla teksti salasana, nyt ei ollut. Kysyit: " + out,
                out.contains("salasana"));

        assertTrue("Syötettäessä oikea salasana tulosteessasi pitäisi olla teksti \"Oikein!\", nyt ei ollut.  Varmista, ettet ole muuttanut salasanaa \"porkkana\":sta. Tulosteesi oli: " + out,
                out.contains("ikein"));
    }

    @Test
    @Points("22.2")
    public void ohjelmaKysyyUudelleenKunVaarin() {
        io.setSysIn("saapas\nkala\ndijkstra\n");

        boolean poikkeusNahty = false;
        try {
            Salasana.main(new String[0]);
        } catch (NoSuchElementException e) {
            poikkeusNahty = true;
        }

        if (!poikkeusNahty) {
            fail("Ohjelman pitäisi jatkaa kyselyä kunnes käyttäjä antaa oikean salasanan.");
        }

        String out = io.getSysOut();
        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);
        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Kysymyksessäsi pitäisi olla teksti salasana, nyt ei ollut. Kysyit: " + out,
                out.contains("salasana"));

        assertTrue("Tulosteessasi ei pitäisi olla tekstiä \"Oikein!\", nyt oli.  Varmista ettet ole muuttanut salasanaa \"porkkana\":sta. Tulosteesi oli: " + out,
                !out.contains("ikein"));

        String[] rivit = out.split("\n");
        String vikarivi = rivit[rivit.length - 1];

        assertTrue("Ohjelman pitäisi kysellä salasanaa kunnes se saa oikean salasanan.", vikarivi.contains(":"));
    }

    @Test
    @Points("22.2")
    public void ohjelmaKysyyKunnesOikein() {
        io.setSysIn("saapas\nkala\ndijkstra\nporkkana\n");
        Salasana.main(new String[0]);

        String out = io.getSysOut();
        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);
        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Kysymyksessäsi pitäisi olla teksti salasana, nyt ei ollut. Kysyit: " + out,
                out.contains("salasana"));

        assertTrue("Tulosteessasi pitäisi olla teksti \"Oikein!\", nyt ei ollut.  Varmista ettet ole muuttanut salasanaa \"porkkana\":sta. Tulosteesi oli: " + out,
                out.contains("ikein"));
    }

    @Test
    @Points("22.2")
    public void ohjelmaLopettaaKyselynKunOikein() {
        io.setSysIn("saapas\nkala\nporkkana\ndijkstra\n");
        Salasana.main(new String[0]);

        String out = io.getSysOut();
        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);
        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Kysymyksessäsi pitäisi olla teksti salasana, nyt ei ollut. Kysyit: " + out,
                out.contains("salasana"));

        assertTrue("Tulosteessasi pitäisi olla teksti \"Oikein!\", nyt ei ollut.  Varmista ettet ole muuttanut salasanaa \"porkkana\":sta. Tulosteesi oli: " + out,
                out.contains("ikein"));

        int oikeinVikaIndeksi = out.lastIndexOf("ikein");
        int vaarinVikaIndeksi = out.lastIndexOf("äärin");

        assertTrue("Ohjelman pitäisi lopettaa salasanan kysely kun sille syötetään oikea salasana.", oikeinVikaIndeksi > vaarinVikaIndeksi);
    }

    @Test
    @Points("22.3")
    public void lopussaSalaisuus() {
        io.setSysIn("saapas\nkala\nporkkana\ndijkstra\n");
        Salasana.main(new String[0]);

        String out = io.getSysOut();
        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);
        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        assertTrue("Kysymyksessäsi pitäisi olla teksti salasana, nyt ei ollut. Kysyit: " + out,
                out.contains("salasana"));

        assertTrue("Tulosteessasi pitäisi olla teksti \"Oikein!\", nyt ei ollut.  Varmista ettet ole muuttanut salasanaa \"porkkana\":sta. Tulosteesi oli: " + out,
                out.contains("ikein"));

        int oikeinVikaIndeksi = out.lastIndexOf("ikein") + "ikein".length();
        int vaarinVikaIndeksi = out.lastIndexOf("äärin") + "äärin".length();

        assertTrue("Ohjelman pitäisi lopettaa salasanan kysely kun sille syötetään oikea salasana.", oikeinVikaIndeksi > vaarinVikaIndeksi);

        assertTrue("Kirjoita ohjelman loppuun salaisuus!", oikeinVikaIndeksi + 4 < out.length());
    }
}
