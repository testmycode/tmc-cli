import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.*;
import static org.junit.Assert.*;

@Points("36.4")
public class Osa4Test {
    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi() {
        int[][] syotteet = {
            {1, -1, 1},
            {2, 6, -1, 4},
            {2, 6, 5, 7, -1, 5}, 
            {6, 1, 4, 7, 4, 8, -1, 5}
        };

        for (int i = 0; i < syotteet.length; i++) {
            tarkasta(syotteet[i], "");
        }
    }

    @Test
    public void testi2() {
        int[] syotteet = {2, 5, -1, 0};
        int oldOut = io.getSysOut().length();
        io.setSysIn(stringiksi(syotteet));
        callMain(SilmukatLopetusMuistaminen.class);
        String out = io.getSysOut().substring(oldOut);

        String virheIlm = "Syötteellä " + stringiksiValilla(syotteet)
                + " pitäisi tulostaa \"Keskiarvo: 3.5\", mutta tulostit " +
                rivi(out, "kiarvo");
        assertTrue("et tulosta mitään!", out.length() > 0);
        assertTrue(virheIlm, out.contains("3.5"));
    }

    private void tarkasta(int[] syotteet, String mj) {
        int oldOut = io.getSysOut().length();
        io.setSysIn(stringiksi(syotteet));
        callMain(SilmukatLopetusMuistaminen.class);
        String out = io.getSysOut().substring(oldOut);
        int odotettu = tulos(syotteet);

        String virheIlm = "Syötteellä " + stringiksiValilla(syotteet)
                + " pitäisi tulostaa \"" + mj + ": " + odotettu +
                ".0\", mutta tulostit " + rivi(out, "kiarvo");
        assertTrue("et tulosta mitään!", out.length() > 0);
        assertTrue(virheIlm, out.contains(odotettu+".0"));
    }

    private void callMain(Class kl) {
        try {
            kl = ReflectionUtils.newInstanceOfClass(kl);
            String[] t = null;
            String x[] = new String[0];
            Method m = ReflectionUtils.requireMethod(kl, "main", x.getClass());
            ReflectionUtils.invokeMethod(Void.TYPE, m, null, (Object) x);
        } catch (NoSuchElementException e) {
            fail("muista lopettaa kun käyttäjä antaa syötteen -1");
        } catch (Throwable e) {
            fail("odottamaton ongelma, et kai jaa ohjelmassa nollalla?");
        }
    }

    private String stringiksi(int[] taulukko) {
        String tuloste = "";
        for (int i = 0; i < taulukko.length - 1; i++) {
            tuloste += taulukko[i] + "\n";

        }

        return tuloste;
    }

    private String stringiksiValilla(int[] taulukko) {
        String tuloste = "";
        for (int i = 0; i < taulukko.length - 1; i++) {
            tuloste += taulukko[i] + " ";
        }

        return tuloste;
    }

    private int tulos(int[] syotteet) {
        return syotteet[syotteet.length - 1];
    }

    private String rivi(String out, String mj) {
        for (String rivi : out.split("\n")) {
            if (rivi.toLowerCase().contains(mj.toLowerCase())) {
                return rivi;
            }
        }

        fail("Ohjelmasi pitäisi tulostaa lukujen keskiarvo");
        return "";
    }
}
