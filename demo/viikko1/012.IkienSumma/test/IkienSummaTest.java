
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

@Points("12")
public class IkienSummaTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void vainYksiNumero(){
        ReflectionUtils.newInstanceOfClass(IkienSumma.class);
        io.setSysIn("arto\n1\npekka\n2\n");
        try {
            IkienSumma.main(new String[0]);
        } catch (NumberFormatException e) {
            fail("Tarkista että asetat luet syötteet oikeassa järjestyksessä. Ensin nimi, sitten ikä, sitten taas nimi, ja lopuksi ikä.");
        }

        String out = io.getSysOut();
        Pattern pattern = Pattern.compile("[^\\d]*(\\d+).*[^\\d]*(\\d+).*",
                Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(out);
        if ( matcher.matches() )
            fail("Tulosteessasi ei saa olla muita numeroita kuin ikien summa. Tulosteesi oli: " + out);
    }
    
    @Test
    public void test() {
        testIkienSumma("Matti", 2, "Arto", 1);
    }
    
    @Test
    public void testToinen() {
        testIkienSumma("Teodor", 0, "Bart", 13);
    }

    private void testIkienSumma(String ekanNimi, int ekanIka, String tokanNimi, int tokanIka) {
        ReflectionUtils.newInstanceOfClass(IkienSumma.class);
        io.setSysIn(ekanNimi + "\n" + ekanIka + "\n" + tokanNimi + "\n" + tokanIka + "\n");
        try {
            IkienSumma.main(new String[0]);
        } catch (NumberFormatException e) {
            fail("Tarkista että asetat luet syötteet oikeassa järjestyksessä. Ensin nimi, sitten ikä, sitten taas nimi, ja lopuksi ikä.");
        }

        String out = io.getSysOut();

        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);

        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        String nimet = ekanNimi + " ja " + tokanNimi;
        assertTrue("Tulosteessasi pitäisi olla teksti \"" + nimet + "\", nyt ei ollut. Tulosteesi oli: " + out,
                out.contains(nimet));

        Pattern pattern = Pattern.compile("[^\\d]*(\\d+).*",
                Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(out);
        if (!matcher.matches()) {
            fail("Tulosteessasi pitäisi olla ikien summa lukuna, nyt ei ollut. Tulosteesi oli: " + out);
            return;
        }

        int summa = Integer.parseInt(matcher.group(1));

        assertTrue("Ikien " + ekanIka + " ja " + tokanIka + " summaksi pitäisi tulla " + (ekanIka + tokanIka) + ", tulostit: " + out
            , (ekanIka + tokanIka) == summa);
    }
}
