import fi.helsinki.cs.tmc.edutestutils.MockInOut;
import fi.helsinki.cs.tmc.edutestutils.Points;
import java.lang.reflect.Field;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import static org.powermock.api.easymock.PowerMock.*;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest(MontaTulostusta.class)
@Points("38")
public class MontaTulostustaTest {
    @Rule
    public PowerMockRule p = new PowerMockRule();

    @Test
    public void testaaEtteiKenttia() {
        Field[] fs = MontaTulostusta.class.getDeclaredFields();
        if (fs.length != 0) {
            fail("Luokassa MontaTulostusta on kenttä nimeltään "+fs[0].getName()+" poista se!");
        }
    }

    @Test
    public void testaaMetodi() {
        MockInOut mio = new MockInOut("");
        try {
            MontaTulostusta.tulostaTeksti();
            assertEquals("Et tulostanut oikeaa merkkijonoa metodista tulostaTeksti()!",
                         "Alussa olivat suo, kuokka ja Java.", mio.getOutput().trim());
        } catch (Throwable t) {
            fail("Jokin meni pieleen kun kutsuttiin metodia tulostaTeksti(). Varmista että metodi vain tulostaa \"Alussa olivat suo, kuokka ja Java.\". Lisätietoja: "+t);
        }
        mio.close();
    }

    @Test
    public void testaaTulostaaJotain() {
        MockInOut mio = new MockInOut("3\n\n");
        MontaTulostusta.main(null);
        assertFalse("Et tulostanut mitään!", mio.getOutput().isEmpty());
        mio.close();
    }

    @Test
    public void testaaTulostaaYhdenOikein() throws Exception {
        MockInOut mio = new MockInOut("1\n");

        MontaTulostusta.main(null);

        String out = mio.getOutput();
        String[] rivit = out.split("\n");
        assertEquals("Et tulostanut oikeaa merkkijonoa!",
                "Kuinka monta?", rivit[0].trim());
        assertEquals("Tulostit liian vähän rivejä kun syöte oli \"1\".", 2, rivit.length);
        assertEquals("Et tulostanut oikeaa merkkijonoa!",
                "Alussa olivat suo, kuokka ja Java.",
                rivit[1].trim());
        mio.close();
    }

    @Test
    public void testaaTulostaaMontaOikein() throws Exception {
        MockInOut mio = new MockInOut("9\n");

        MontaTulostusta.main(null);

        String out = mio.getOutput();
        String[] rivit = out.split("\n");
        assertEquals("Et tulostanut oikeaa merkkijonoa!",
                "Kuinka monta?", rivit[0].trim());
        assertEquals("Tulostit liian vähän rivejä kun syöte oli \"9\".", 10, rivit.length);
        for (int i = 1; i < rivit.length; i++) {
            assertEquals("Et tulostanut oikeaa merkkijonoa!",
                    "Alussa olivat suo, kuokka ja Java.",
                    rivit[i].trim());
        }

        mio.close();
    }

    @Test
    public void kutsuuOikeaaMetodia() throws Exception {
        MockInOut mio = new MockInOut("2\n");

        mockStaticPartial(MontaTulostusta.class, "tulostaTeksti");
        MontaTulostusta.tulostaTeksti();
        MontaTulostusta.tulostaTeksti();

        replay(MontaTulostusta.class);

        MontaTulostusta.main(null);

        try {
            verifyAll();
        } catch (AssertionError e) {
            fail("Et kutsu metodia tulostaTeksti kahta kertaa kun syöte on 2. Käytä metodia tulostaTeksti viestin tulostamiseen. Lisätietoja: "+e);
        }
        mio.close();
    }
}