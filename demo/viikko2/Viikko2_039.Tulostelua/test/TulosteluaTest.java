
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockInOut;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import static org.powermock.api.easymock.PowerMock.*;

import java.lang.reflect.Field;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest(Tulostelua.class)
public class TulosteluaTest {

    @Rule
    public PowerMockRule p = new PowerMockRule();

    public String sanitize(String s) {
        return s.replace("\r\n", "\n").replace("\r", "\n");
    }

    @Points("39.1 39.2 39.3 39.4")
    @Test
    public void testaaEtteiKenttia() {
        Field[] fs = Tulostelua.class.getDeclaredFields();
        if (fs.length != 0) {
            fail("Luokassa Tulostelua on kenttä nimeltään " + fs[0].getName() + " poista se!");
        }
    }

    @Test
    @Points("39.1")
    public void testaaTulostaTahtia1() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaTahtia(3);
        String out = sanitize(mio.getOutput());
        assertTrue("Et tulostanut yhtään merkkiä \"*\" kun kutsuttiin tulostaTahtia.", out.contains("*"));
        assertTrue("Et tulostanut yhtään rivinvaihtoa kun kutsuttiin tulostaTahtia.", out.contains("\n"));
        assertEquals("Tulostit väärin kun kutsuttiin tulostaTahtia(3).", "***\n", out);
        mio.close();
    }

    @Test
    @Points("39.1")
    public void testaaTulostaTahtia2() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaTahtia(7);
        String out = sanitize(mio.getOutput());
        assertEquals("Tulostit väärin kun kutsuttiin tulostaTahtia(7).", "*******", out.trim());
    }

    @Test
    @Points("39.2")
    public void testaaTulostaNelio1() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaNelio(3);
        String out = sanitize(mio.getOutput());
        assertEquals("Tulostit väärin kun kutsuttiin tulostaNelio(3).", "***\n***\n***", out.trim());
    }

    @Test
    @Points("39.2")
    public void testaaTulostaNelio2() {

        mockStaticPartial(Tulostelua.class, "tulostaTahtia");
        Tulostelua.tulostaTahtia(4);
        Tulostelua.tulostaTahtia(4);
        Tulostelua.tulostaTahtia(4);
        Tulostelua.tulostaTahtia(4);

        replay(Tulostelua.class);

        try {
            Tulostelua.tulostaNelio(4);
            verifyAll();
        } catch (AssertionError e) {
            fail("Et kutsu metodia tulostaTahtia. Metodikutsun tulostaNelio(4) pitäisi kutsua neljä kertaa tulostaTahtia(4). Lisätietoja: " + e);
        }
    }

    @Test
    @Points("39.3")
    public void testaaTulostaSuorakulmio1() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaSuorakulmio(4, 2);
        String out = sanitize(mio.getOutput());
        assertEquals("Tulostit väärin kun kutsuttiin tulostaSuorakulmio(4,2).", "****\n****", out.trim());
    }

    @Test
    @Points("39.3")
    public void testaaTulostaSuorakulmio2() {

        mockStaticPartial(Tulostelua.class, "tulostaTahtia");
        Tulostelua.tulostaTahtia(5);
        Tulostelua.tulostaTahtia(5);
        Tulostelua.tulostaTahtia(5);
        Tulostelua.tulostaTahtia(5);

        replay(Tulostelua.class);

        try {
            Tulostelua.tulostaSuorakulmio(5, 4);
            verifyAll();
        } catch (AssertionError e) {
            fail("Et kutsu metodia tulostaTahtia. Metodikutsun tulostaSuorakulmio(5,4) pitäisi kutsua neljä kertaa tulostaTahtia(5). Lisätietoja: " + e);
        }
    }

    @Test
    @Points("39.4")
    public void testaaTulostaKolmio1() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaKolmio(3);
        String out = sanitize(mio.getOutput());
        assertEquals("Tulostit väärin kun kutsuttiin tulostaKolmio(3).", "*\n**\n***", out.trim());
    }

    @Test
    @Points("39.4")
    public void testaaTulostaKolmio2() {

        mockStaticPartial(Tulostelua.class, "tulostaTahtia");
        Tulostelua.tulostaTahtia(1);
        Tulostelua.tulostaTahtia(2);

        replay(Tulostelua.class);

        try {
            Tulostelua.tulostaKolmio(2);
            verifyAll();
        } catch (AssertionError e) {
            if (e.toString().contains("Unexpected method call Tulostelua.tulostaTahtia(0):")) {
                fail("Metodikutsun tulostaKolmio(2) pitäisi kutsua ensin tulostaTahtia(1) ja sen jälkeen tulostaTahtia(2). "
                        + "\nKutsuit kuitenkin aluksi tulostaTahtia(0). Muuta ohjelmaasi siten että tämä turha kutsu poistuu.");
            }
            fail("Metodikutsun tulostaKolmio(2) pitäisi kutsua ensin tulostaTahtia(1) ja sen jälkeen tulostaTahtia(2). "
                    + "\n"+e);
        }
    }

    @Test
    @Points("39.4")
    public void testaaTulostaKolmio3() {

        mockStaticPartial(Tulostelua.class, "tulostaTahtia");
        Tulostelua.tulostaTahtia(1);
        Tulostelua.tulostaTahtia(2);
        Tulostelua.tulostaTahtia(3);
        Tulostelua.tulostaTahtia(4);

        replay(Tulostelua.class);

        try {
            Tulostelua.tulostaKolmio(4);
            verifyAll();
        } catch (AssertionError e) {
            fail("Et kutsu metodia tulostaTahtia. Metodikutsun tulostaKolmio(4) pitäisi kutsua neljä kertaa metodia tulostaTahtia. Lisätietoja: " + e);
        }
    }
}