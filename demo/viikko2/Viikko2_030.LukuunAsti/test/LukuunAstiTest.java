import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

@Points("30")
public class LukuunAstiTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void varmistaTulostus() {
        io.setSysIn("3\n");

        ReflectionUtils.newInstanceOfClass(LukuunAsti.class);
        LukuunAsti.main(new String[0]);

        String tulostus = io.getSysOut();
        tulostus = tulostus.replaceAll("[^\\d]", " ");
        tulostus = tulostus.trim();
        tulostus = tulostus.replace("1", "");
        tulostus = tulostus.replace("2", "");
        tulostus = tulostus.replace("3", "");

        tulostus = tulostus.trim();
        if (!tulostus.isEmpty()) {
            fail("Kun tulostat lukuja kolmeen asti, sinun tulee tulostaa vain luvut 1, 2 ja 3. Nyt tulostit: " + tulostus);
        }
    }

    @Test
    public void testi() {
        int[] luvut = {1, 50, 100};
        for (int luku : luvut) {
            testaa(luku);
        }
    }

    private void testaa(int viimeinen) {
        io.setSysIn(viimeinen + "\n");

        ReflectionUtils.newInstanceOfClass(LukuunAsti.class);
        LukuunAsti.main(new String[0]);

        int vikaLuku = otaLukuLopusta(io.getSysOut(), viimeinen);

        if (viimeinen != vikaLuku) {
            fail("Viimeisellä rivillä pitäisi olla luku " + viimeinen + ", nyt oli " + vikaLuku);
        }
    }

    private static int otaLukuLopusta(String inputStr, int viimeinen) {
        String patternStr = "(?s).*?(\\d+)\\s*$";
        Matcher matcher = Pattern.compile(patternStr).matcher(inputStr);
        assertTrue("Tulostuksessa tulee olla numero. Käyttäjän syötteellä "+viimeinen+ " tulostui vain \""+inputStr+"\"", matcher.find());

        int luku = Integer.parseInt(matcher.group(1));
        return luku;
    }
}
