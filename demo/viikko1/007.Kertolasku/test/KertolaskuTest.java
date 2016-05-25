import org.junit.Test;
import org.junit.Rule;
import java.util.regex.*;

import static org.junit.Assert.*;

import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;

@Points("7")
public class KertolaskuTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void test() {
        Kertolasku.main(new String[0]);
        String out = io.getSysOut();

        assertTrue("Et tulostanut mitään!",out.trim().length()>0);

        Matcher m = Pattern.compile("(?s).*?(\\d+)\\s*\\*\\s*(\\d+)\\s*=\\s*(\\d+).*").matcher(out);

        assertTrue("Tulosteesi tulee olla muotoa 'X * Y = Z' jollain numeroilla X, Y ja Z. Nyt se oli: "+out,
                   m.matches());

        String sa = m.group(1).trim();
        String sb = m.group(2).trim();
        String sc = m.group(3).trim();

        int a,b,c;

        try {
            a = Integer.parseInt(sa);
        } catch (NumberFormatException e) {
            fail("Tulosteessasi ollut merkkijono \""+sa+"\" ei ole käypä numero!");
            return; // tyhmä java
        }

        try {
            b = Integer.parseInt(sb);
        } catch (NumberFormatException e) {
            fail("Tulosteessasi ollut merkkijono \""+sb+"\" ei ole käypä numero!");
            return; // tyhmä java
        }

        try {
            c = Integer.parseInt(sc);
        } catch (NumberFormatException e) {
            fail("Tulosteessasi ollut merkkijono \""+sc+"\" ei ole käypä numero!");
            return; // tyhmä java
        }

        assertEquals("Ohjelmasi väitti että "+a+" * "+b+" = "+c+" , mutta näin ei ole!",a*b,c);
    }
}