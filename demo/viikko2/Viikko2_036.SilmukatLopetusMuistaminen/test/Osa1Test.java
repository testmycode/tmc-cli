import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.*;
import static org.junit.Assert.*;

@Points("36.1")
public class Osa1Test {
    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi() {
        int[][] syotteet = {{1, -1}, {2, 5, -1}};

       
        for (int i = 0; i < syotteet.length; i++) {
            int oldOut = io.getSysOut().length();
            io.setSysIn(stringiksi(syotteet[i]));
            callMain(SilmukatLopetusMuistaminen.class);
            String out = io.getSysOut().substring(oldOut);

            String virheIlm = "Kun käyttäjä on antanut luvun -1 pitäisi ohjelman tulostaa ensin"
                    + "\"Syötä luvut:\" ja lopussa \"Kiitos ja näkemiin!\"";
            assertTrue("et tulosta mitään!", out.length() > 0);
            assertTrue(virheIlm, out.contains("luvut"));
            assertTrue(virheIlm, out.contains("iitos"));
        }

    }


    private void callMain(Class kl) {
        try {
            kl = ReflectionUtils.newInstanceOfClass(kl);
            String[] t = null;
            String x[] = new String[0];
            Method m = ReflectionUtils.requireMethod(kl, "main", x.getClass());
            ReflectionUtils.invokeMethod(Void.TYPE, m, null, (Object) x);
        } catch (NoSuchElementException e) {
            fail("muista lopettaa, kun käyttäjä antaa syötteen -1");
        } catch (Throwable e) {
            fail("odottamaton ongelma, et kai jaa ohjelmassa nollalla?");
        }
    }

    private String stringiksi(int[] taulukko) {
        String tuloste = "";
        for (int luku : taulukko) {
            tuloste += luku + "\n";
        }

        return tuloste;
    }
}
