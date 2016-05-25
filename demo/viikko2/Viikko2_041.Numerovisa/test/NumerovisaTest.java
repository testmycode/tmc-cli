
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import fi.helsinki.cs.tmc.edutestutils.MockInOut;
import java.util.NoSuchElementException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.replace;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest(Numerovisa.class)
public class NumerovisaTest {

    @Rule public PowerMockRule p = new PowerMockRule();

    
    @Test
    @Points("41.1")
    public void testiLiianPieni() {
        MockInOut mio = new MockInOut("-1\n");
        int oldOut = mio.getOutput().length();
        ReflectionUtils.newInstanceOfClass(Numerovisa.class);
        try {
            Numerovisa.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }
        String out = mio.getOutput().substring(oldOut);
        assertTrue("Jos annetaan liian pieni luku, ohjelman tulee tulostaa \"Luku on suurempi\".", out.contains("suurempi"));
    }

    @Test
    @Points("41.1")
    public void testiLiianIso() {
        MockInOut mio = new MockInOut("101\n");
        int oldOut = mio.getOutput().length();
        ReflectionUtils.newInstanceOfClass(Numerovisa.class);
        try {
            Numerovisa.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }
        String out = mio.getOutput().substring(oldOut);

        assertTrue("Jos annetaan liian pieni luku, ohjelman tulee tulostaa \"Luku on pienempi\".", out.contains("pienempi"));
    }

    @Test
    @Points("41.1")
    public void testiOikein() {
        MockInOut mio = new MockInOut("1\n");

        ReflectionUtils.newInstanceOfClass(Numerovisa.class);

        replace(method(Numerovisa.class, "arvoLuku")).with(method(Uno.class, "getUno"));
        try {
            Numerovisa.main(new String[0]);
        } catch (Exception e) {
            // toistolausekkeessa useampia kyselyjä
        }


        String out = mio.getOutput();
        mio.close();
        assertTrue("Jos annetaan oikea luku, ohjelman tulee tulostaa \"Onneksi olkoon, oikein arvattu!\".", out.contains("oikein"));
    }

    @Test
    @Points("41.2")
    public void toistuvaTestiAlhaalta() {
        MockInOut mio = new MockInOut("0\n1\n");

        ReflectionUtils.newInstanceOfClass(Numerovisa.class);

        replace(method(Numerovisa.class, "arvoLuku")).with(method(Uno.class, "getUno"));
        try {
            Numerovisa.main(new String[0]);
        } catch (Exception e) {
            // toistolausekkeessa useampia kyselyjä
        }

        String out = mio.getOutput();
        mio.close();

        assertTrue("Jos arvattava luku on 1 ja syötteet ovat 0, 1, tulee ohjelman ensin tulostaa \n"
                + "\"Luku on suurempi\", jonka jälkeen \"Onneksi olkoon, oikein arvattu!\"", 
                !out.contains("pienempi") && out.contains("suurempi") && out.contains("oikein"));
    }

    @Test
    @Points("41.2")
    public void toistoLoppuuOikeaan() {
        MockInOut mio = new MockInOut("0\n1\nzorbas\n");

        ReflectionUtils.newInstanceOfClass(Numerovisa.class);

        replace(method(Numerovisa.class, "arvoLuku")).with(method(Uno.class, "getUno"));
        try {
            Numerovisa.main(new String[0]);
        } catch (NumberFormatException e) {
            fail("Syötteen lukemisen tulee loppua kun käyttäjä syöttää oikean luvun.");
        }

        String out = mio.getOutput();
        mio.close();

        assertTrue("Jos arvattava luku on 1 ja syötteet ovat 0, 1, tulee ohjelman ensin tulostaa\n"
                + "\"Luku on suurempi\", jonka jälkeen \"Onneksi olkoon, oikein arvattu!\"", 
                !out.contains("pienempi") );
    }

    @Test
    @Points("41.2")
    public void toistuvaTestiYlhaalta() {
        MockInOut mio = new MockInOut("2\n1\n");

        ReflectionUtils.newInstanceOfClass(Numerovisa.class);

        replace(method(Numerovisa.class, "arvoLuku")).with(method(Uno.class, "getUno"));
        try {
            Numerovisa.main(new String[0]);
        } catch (Exception e) {
            // toistolausekkeessa useampia kyselyjä
        }

        String out = mio.getOutput();
        mio.close();

        assertTrue("Jos arvattava luku on 1 ja syötteet ovat 2, 1, tulee ohjelman ensin tulostaa\n"
                + "\"Luku on pienempi\", jonka jälkeen \"Onneksi olkoon, oikein arvattu!\"", 
                !out.contains("suurempi") && out.contains("pienempi") && out.contains("oikein"));
    }

    @Test
    @Points("41.3")
    public void lasketaanArvauskertoja() {
        ReflectionUtils.newInstanceOfClass(Numerovisa.class);

        MockInOut mio = new MockInOut("5\n4\n3\n2\n1\n");

        replace(method(Numerovisa.class, "arvoLuku")).with(method(Uno.class, "getUno"));
        try {
            Numerovisa.main(new String[0]);
        } catch (Exception e) {
            // toistolausekkeessa useampia kyselyjä
        }

        String out = mio.getOutput();
        mio.close();

        out = out.replaceAll("[^\\d]+", " ").trim();
        out = out.replaceAll("\\s+", " ");

        String[] numerot = out.split("\\s+");
        if (numerot.length == 1 && numerot[0].trim().isEmpty()) {
            fail("Sinun pitäisi tulostaa arvauskertojen lukumäärä.");
        }
        
        if(numerot.length < 4) {
            fail("Sinun pitäisi tulostaa arvauskertojen lukumäärä.");
        }

        int edellinen = Integer.parseInt(numerot[numerot.length - 1]);
        for (int i = numerot.length - 2; i >= 0; i--) {
            int luku = Integer.parseInt(numerot[i]);
            if (edellinen <= luku) {
                fail("Arvausten määrän tulisi kasvaa.");
            }
        }
    }
}
