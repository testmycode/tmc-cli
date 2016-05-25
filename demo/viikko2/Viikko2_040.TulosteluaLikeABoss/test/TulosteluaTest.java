import fi.helsinki.cs.tmc.edutestutils.MockInOut;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;

import org.powermock.core.classloader.annotations.PrepareForTest;
import static org.powermock.api.easymock.PowerMock.*;

import java.lang.reflect.Field;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest(Tulostelua.class)
public class TulosteluaTest {
    //static {
    //mockStaticPartial(Tulostelua.class, "tulostaTahtia", "tulostaTyhjaa");
    //}

    @Rule
    public PowerMockRule p = new PowerMockRule();
    
    public String sanitize(String s) {
        return s.replace("\r\n","\n").replace("\r","\n");
    }

    @Test
    @Points("40.1 40.2 40.3")
    public void testaaEtteiKenttia() {
        Field[] fs = Tulostelua.class.getDeclaredFields();
        if (fs.length!=0) {
            fail("Luokassa Tulostelua on kenttä nimeltään "+fs[0].getName()+" poista se!");
        }
    }

    @Test
    @Points("40.1")
    public void testaaTulostaTahtia() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaTahtia(3);
        String out = sanitize(mio.getOutput());
        assertTrue("Et tulostanut yhtään merkkiä \"*\" kun kutsuttiin tulostaTahtia.",out.contains("*"));
        assertTrue("Et tulostanut yhtään rivinvaihtoa kun kutsuttiin tulostaTahtia.",out.contains("\n"));
        assertEquals("Tulostit väärin kun kutsuttiin tulostaTahtia(3).","***\n",out);
        mio.close();
    }
    
    @Test
    @Points("40.1")
    public void testaaTulostaTyhjaa1() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaTyhjaa(1);
        String out = sanitize(mio.getOutput());
        assertEquals("Metodikutsun tulostaTyhjaa(1) pitäisi tulostaa 1 merkki, nyt merkkejä tulostui",1,out.length());
        assertFalse("Metodinkutsun tulostaTyhjaa(1) ei pitäisi tulostaa rivinvaihtoa, nyt kuitenkin niin tapahtuu",out.contains("\n"));
        assertEquals("Tarkasta metodikutsu tulostaTyhjaa(1)", " ", out);
        mio.close();
    }
    
    @Test
    @Points("40.1")
    public void testaaTulostaTyhjaa2() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaTyhjaa(3);
        String out = sanitize(mio.getOutput());
        assertEquals("Metodikutsun tulostaTyhjaa(3) pitäisi tulostaa 3 merkkiä, nyt merkkejä tulostulostui",3,out.length());
        assertFalse("Metodinkutsun tulostaTyhjaa(3) ei pitäisi tulostaa rivinvaihtoa, nyt kuitenkin niin tapahtuu",out.contains("\n"));
        assertEquals("Tarkasta metodikutsu tulostaTyhjaa(3)", "   ", out);
        mio.close();
    }
    
    @Test
    @Points("40.2")
    public void testaaTulostaPieniKolmio() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaKolmio(1);
        String out = sanitize(mio.getOutput());
        assertFalse("Et kai laita rivien alkuun ylimäräistä välilyöntiä?",out.startsWith(" "));
        assertEquals("Tulostit väärin kun kutsuttiin tulostaKolmio(1).","*\n",out);
        mio.close();
    }  
    
    @Test
    @Points("40.2")
    public void testaaTulostaKolmio() {
        MockInOut mio = new MockInOut("");
        Tulostelua.tulostaKolmio(3);
        String out = sanitize(mio.getOutput());
        assertEquals("Tulostit väärin kun kutsuttiin tulostaKolmio(3).","  *\n **\n***\n",out);
        mio.close();
    }

    @Test
    @Points("40.2")
    public void testaaTulostaKolmio2() {
        mockStaticPartial(Tulostelua.class, "tulostaTahtia", "tulostaTyhjaa");
        reset(Tulostelua.class);

        Tulostelua.tulostaTyhjaa(3);
        Tulostelua.tulostaTahtia(1);
        Tulostelua.tulostaTyhjaa(2);
        Tulostelua.tulostaTahtia(2);
        Tulostelua.tulostaTyhjaa(1);
        Tulostelua.tulostaTahtia(3);
        Tulostelua.tulostaTyhjaa(0);
        //expectLastCall().anyTimes();
        Tulostelua.tulostaTahtia(4);

        replay(Tulostelua.class);

        try {
            Tulostelua.tulostaKolmio(4);
            verifyAll();
        } catch (AssertionError e) {
            fail("Metodin tulostaKolmio pitäisi kutsua metodeja tulostaTyhjaa ja tulostaTahtia. Lisätietoja: "+e);
        }
    }

    @Test
    @Points("40.3")
    public void testaaJouluKuusi0() {
        MockInOut mio = new MockInOut("");
        Tulostelua.jouluKuusi(4);
        String out = sanitize(mio.getOutput());
        assertTrue("kutsuttaessa jouluKuusi(4), pitäisi ensimmäisellä rivillä olla alussa 3 tyhjää ja sen jälkeen tähti, tarkasta ettei tyhjää ole liikaa tai liian vähän",
                     out.startsWith("   *"));

        mio.close();
    }   
    
    @Test
    @Points("40.3")
    public void testaaJouluKuusi1() {
        MockInOut mio = new MockInOut("");
        Tulostelua.jouluKuusi(4);
        String out = sanitize(mio.getOutput());
        assertEquals("Tulostit väärin kun kutsuttiin jouluKuusi(4).",
                     "   *\n  ***\n *****\n*******\n  ***\n  ***\n",out);

        mio.close();
    }

    @Test
    @Points("40.3")
    public void testaaJouluKuusi2() {
        MockInOut mio = new MockInOut("");
        Tulostelua.jouluKuusi(7);
        String out = sanitize(mio.getOutput());
        assertEquals("Tulostit väärin kun kutsuttiin jouluKuusi(7).",
                     "      *\n"+
                     "     ***\n"+
                     "    *****\n"+
                     "   *******\n"+
                     "  *********\n"+
                     " ***********\n"+
                     "*************\n"+
                     "     ***\n"+
                     "     ***\n",
                     out);

        mio.close();
    }

    @Test
    @Points("40.3")
    public void testaaJouluKuusi3() {
        MockInOut mio = new MockInOut("");

        mockStaticPartial(Tulostelua.class, "tulostaTahtia", "tulostaTyhjaa");
        reset(Tulostelua.class);
        Tulostelua.tulostaTahtia(1);
        Tulostelua.tulostaTahtia(3);
        Tulostelua.tulostaTahtia(5);
        Tulostelua.tulostaTahtia(7);
        Tulostelua.tulostaTahtia(9);
        Tulostelua.tulostaTahtia(3);
        Tulostelua.tulostaTahtia(3);

        Tulostelua.tulostaTyhjaa(0);
        //expectLastCall().anyTimes();
        Tulostelua.tulostaTyhjaa(4);
        Tulostelua.tulostaTyhjaa(3);
        Tulostelua.tulostaTyhjaa(2);
        Tulostelua.tulostaTyhjaa(1);
        Tulostelua.tulostaTyhjaa(3);
        Tulostelua.tulostaTyhjaa(3);

        replay(Tulostelua.class);

        try {
            Tulostelua.jouluKuusi(5);
            verifyAll();
        } catch (AssertionError e) {
            fail("Tulosta kuusi vain käyttäen metodeja tulostaTyhjaa ja tulostaTahtia. Nyt ei mennyt oikein! Lisätietoja: "+e);
        }

        assertEquals("Alä tulosta mitään suoraan metodista jouluKuusi. Käytä vain metodeja tulostaTyhjaa ja tulostaTahtia. Nyt tulostit: \""+mio.getOutput()+"\"","",mio.getOutput());

        mio.close();
    }
}
