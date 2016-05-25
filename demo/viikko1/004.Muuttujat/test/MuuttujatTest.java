import org.junit.Test;
import org.junit.Rule;
import org.junit.Before;
import java.util.regex.*;

import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;

@Points("4")
public class MuuttujatTest {

    String out;

    @Rule
    public MockStdio io = new MockStdio();

    @Before
    public void sieppaaTulostus() {
        Muuttujat.main(new String[0]);
        out = io.getSysOut();
    }

    String ekaRegex(String a, String b) {
        return "(?s).*"+a+"\\s*"+b+"\\s.*";
    }

    String tokaRegex(String a, String b) {
        return "(?s).*tiivistelmä:.*\\s*"+b+"\\s.*";
    }

    void testaa(String a, String b) {
        assertTrue("Tarkasta että "+a+" -tulostus on oikein!",
                   Pattern.matches(ekaRegex(a,b),out));
        assertTrue("Tarkasta että "+a+" -tulostus on oikein myös tiivistelmässä!",
                   Pattern.matches(tokaRegex(a,b),out));
    }

    @Test
    public void testaaKanat() {
        testaa("Kanoja:","9000");
    }

    @Test
    public void testaaPekoni() {
        testaa("Pekonia \\(kg\\):","0\\.1");
    }

    @Test
    public void testaaTraktori() {
        testaa("Traktori:","Zetor");
    }

}