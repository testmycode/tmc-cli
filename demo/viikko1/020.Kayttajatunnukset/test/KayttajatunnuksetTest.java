
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import org.junit.*;
import static org.junit.Assert.*;

@Points("20")
public class KayttajatunnuksetTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void sopivatKayvat() {
        String[][] tunnusSalasana = {{"aleksi", "tappara"}, {"elina", "kissa"}};

        for (String[] pari : tunnusSalasana) {
            sopivaKay(pari[0], pari[1]);
        }
    }

    @Test
    public void sopimattomatEivatKay() {
        String[][] tunnusSalasana = {
            {"arto", "salaisuus"}, 
            {"aleksi", ""}, 
            {"aleksi", "ilves"}, 
            {"Elina", "kissa"},
            {"elina", "koira"},
            {"", "kissa"}
        };

        for (String[] pari : tunnusSalasana) {
            sopimatonEiKay(pari[0], pari[1]);
        }
    }

    private void sopivaKay(String k, String s) {
        int oldOut = io.getSysOut().length();
        io.setSysIn(k + "\n" + s + "\n");
        callMain(Kayttajatunnukset.class);
        String out = io.getSysOut().substring(oldOut);

        assertTrue("et tulosta mitään!", out.length() > 0);

        assertTrue("syötteellä tunnus: \"" + k + "\" salasana: \"" + s + "\" tulostit \"" + out + "\". Muista, että merkkijonoja ei voi vertailla ==:lla vaan pitää käyttää equals:ia!", out.toLowerCase().contains("kirj"));;
        assertTrue("syötteellä tunnus: \"" + k + "\" salasana: \"" + s + "\" tulostit \"" + out + "\". Muista, että merkkijonoja ei voi vertailla ==:lla vaan pitää käyttää equals:ia!", !out.toLowerCase().contains("virh"));
    }

    private void sopimatonEiKay(String k, String s) {
        int oldOut = io.getSysOut().length();
        io.setSysIn(k + "\n" + s + "\n");
        callMain(Kayttajatunnukset.class);
        String out = io.getSysOut().substring(oldOut);

        assertTrue("et tulosta mitään!", out.length() > 0);

        assertTrue("syötteellä tunnus: \"" + k + "\" salasana: \"" + s + "\" tulostit \"" + out + "\". Muista, että merkkijonoja ei voi vertailla ==:lla vaan pitää käyttää equals:ia!", !out.toLowerCase().contains("kirj"));
        assertTrue("syötteellä tunnus: \"" + k + "\" salasana: \"" + s + "\" tulostit \"" + out + "\". Muista ,että merkkijonoja ei voi vertailla ==:lla vaan pitää käyttää equals:ia!", out.toLowerCase().contains("virh"));
    }

    private void callMain(Class kl) {
        try {
            kl = ReflectionUtils.newInstanceOfClass(kl);
            String[] t = null;
            String x[] = new String[0];
            Method m = ReflectionUtils.requireMethod(kl, "main", x.getClass());
            ReflectionUtils.invokeMethod(Void.TYPE, m, null, (Object) x);
        }  catch (NoSuchElementException e) {
            fail("luethan syöteen käyttäjältä lukija.nextLine()-komennolla?");
        } catch (Throwable e) {
            fail("Jotain kummallista tapahtui. Saattaa olla että "+kl + "-luokan public static void main(String[] args) -metodi on hävinnyt\n"
                    + "tai ohjelmasi kaatui poikkeukseen. Lisätietoja "+e);
        }
    }
}
