import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.*;
import static org.junit.Assert.*;

@Points("32")
public class LukusarjanSummaTest {
    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi() {
        int[][] syotteet = {{3,6}, {4,10}, {5,15}, {10,55}};

        for (int i = 0; i < syotteet.length; i++) {
            tarkista(syotteet[i][0], syotteet[i][1]);
            
        }                
    }

    private void tarkista(int vika, int odotettuVastaus) {
        int eka = 1;
        int oldOut = io.getSysOut().length();
        io.setSysIn(vika + "\n");
        callMain(LukusarjanSumma.class);
        String out = io.getSysOut().substring(oldOut);

        int vastaus = otaLukuLopusta(out);
        
        String virheIlm = "summan " + eka + ".." + vika + " pitäisi olla " +
                odotettuVastaus + ", mutta tulostit \"" + out + "\"";
        assertTrue("et tulosta mitään!", out.length() > 0);
        assertEquals(virheIlm, odotettuVastaus, vastaus);
        
        assertFalse("Hmm.. summan pitäisi olla positiivinen luku, nyt tulostit "+out,out.contains("-"+odotettuVastaus));
    }    
    
    private void callMain(Class kl) {
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

    private static int otaLukuLopusta(String inputStr) {
        String patternStr = "(?s).*?(\\d+)\\s*$";

        Matcher matcher = Pattern.compile(patternStr).matcher(inputStr);

        assertTrue("tulostuksen pitäisi olla muotoa \"Summa on 10\"",matcher.find());

        int luku = Integer.parseInt(matcher.group(1));
        return luku;
    }
}
