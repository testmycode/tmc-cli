
import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static org.powermock.api.easymock.PowerMock.*;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import java.util.NoSuchElementException;
import junit.framework.AssertionFailedError;
import org.junit.Before;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Kuvaaja.class)
public class LampotilojaTest {

    @Rule
    public MockStdio io = new MockStdio();
    Kuvaaja k;

    @Before
    public void before() {
        k = createStrictMock(Kuvaaja.class);
        Kuvaaja.instance = k;
    }

    void ajaMain() {
        replayAll();
        try {
            Lampotiloja.main(new String[0]);
        } catch (NoSuchElementException e) {
            // pass
        }
    }

    void tarkasta(String viesti) {
        try {
            ajaMain();
            verifyAll();
        } catch (AssertionError t) {
            fail(viesti + " Lisätietoja: " + t);
        } catch (Throwable t) {
            fail("Jokin meni pieleen: "+t);
        }
    }

    @Points("23.1")
    @Test
    public void testaaYksi() {
        io.setSysIn("3\n");
        k.lisaaNumero_(3);
        tarkasta("Ohjelmasi ei kutsunut Kuvaaja.lisaaNumero(3) kun syöte oli \"3\".");
    }

    @Points("23.1")
    @Test
    public void testaaUseampi() {
        io.setSysIn("3\n4\n5\n");

        //String out = io.getSysOut();

        k.lisaaNumero_(3);
        k.lisaaNumero_(4);
        k.lisaaNumero_(5);

        tarkasta("Ohjelmasi ei tehnyt oikeita Kuvaaja.lisaaNumero()-kutsuja kun syötteet olivat 3, 4 ja 5.");
    }

    @Points("23.2")
    @Test
    public void testaaLiianPieniLampotila() {
        io.setSysIn("-1000\n");
        tarkasta("Ohjelmasi ei jättänyt lämpötilaa -1000 lisäämättä kuvaajaan.");
    }

    @Points("23.2")
    @Test
    public void testaaLiianSuuriLampotila() {
        io.setSysIn("1000\n");
        tarkasta("Ohjelmasi ei jättänyt lämpötilaa 1000 lisäämättä kuvaajaan.");
    }

    @Points("23.2")
    @Test
    public void testaaKaikki() {
        io.setSysIn("0\n-31\n-40\n15\n16\n39\n41\n49\n0\n");
        k.lisaaNumero_(0);
        k.lisaaNumero_(15);
        k.lisaaNumero_(16);
        k.lisaaNumero_(39);
        k.lisaaNumero_(0);
        tarkasta("Kun syötteet ovat 0, -31, -40, 15, 16, 39, 41, 49, 0, ohjelmasi pitäisi lisätä kuvaajaan vain 0, 15, 16, 0.");
    }
}   
   
