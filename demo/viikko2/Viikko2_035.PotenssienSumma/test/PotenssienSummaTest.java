
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.*;
import static org.junit.Assert.*;

@Points("35")
public class PotenssienSummaTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi() {
        int[][] syotteet = {{2, 7}, {5, 63}, {7, 255}, {9, 1023}, {12, 8191}};

        for (int i = 0; i < syotteet.length; i++) {
            tarkista(syotteet[i][0], syotteet[i][1]);
        }
    }

    public void tarkista(int luku, int odotettuVastaus) {
        io.disable();
        io.enable();
        
        io.setSysIn(luku + "\n");
        callMain(PotenssienSumma.class);
        String out = io.getSysOut();

        assertTrue("Et tulosta mitään!", out.length() > 0);

        int vastaus = otaLukuLopusta(out);

        String virheIlm = "Syötteellä " + luku + " tuloksen pitäisi olla " + odotettuVastaus + " tulostit \"" + out + "\"";
        assertEquals(virheIlm, odotettuVastaus, vastaus);
    }

    public static void callMain(Class kl) {
        try {
            kl = ReflectionUtils.newInstanceOfClass(kl);
            String[] t = null;
            String x[] = new String[0];
            Method m = ReflectionUtils.requireMethod(kl, "main", x.getClass());
            ReflectionUtils.invokeMethod(Void.TYPE, m, null, (Object) x);
        } catch (NoSuchElementException e) {
            fail("muista lukea syöte nextLine()-metodilla\n"
                    + "lue syöte vain yhteen kertaan");
        } catch (Throwable e) {
            fail(kl + "-luokan public static void main(String[] args) -metodi on hävinnyt "
                    + "tai jotain muuta odottamatonta tapahtunut, lisätietoja "+e);
        }
    }

    public static int otaLukuLopusta(String inputStr) {

        String patternStr = "(?s).*?(\\d+)\\s*$";

        Matcher matcher = Pattern.compile(patternStr).matcher(inputStr);

        assertTrue("Tulostuksen pitäisi olla muotoa \"Tulos on 6\". Tulostit: "+inputStr, matcher.find());

        int luku = Integer.parseInt(matcher.group(1));
        return luku;
    }
}
