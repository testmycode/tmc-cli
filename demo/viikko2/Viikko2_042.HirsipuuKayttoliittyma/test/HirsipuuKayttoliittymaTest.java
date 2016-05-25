import fi.helsinki.cs.tmc.edutestutils.MockInOut;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import hirsipuu.Hirsipuu;
import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.replace;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest(HirsipuuKayttoliittyma.class)
public class HirsipuuKayttoliittymaTest {
    @Rule
    public PowerMockRule p = new PowerMockRule();

    @Test
    @Points("42.1")
    public void kysytaanKayttajaltaJotain() {
        MockInOut mio = new MockInOut("");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        boolean kysytaanKayttajalta = false;
        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
            kysytaanKayttajalta = true;
        }

        assertTrue("Sinun tulee kysya kayttajalta komentoa.", kysytaanKayttajalta);
    }

    @Test
    @Points("42.1")
    public void whilenEhtoOikein() {
        MockInOut mio = new MockInOut("a\nb\n");
        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // pass
        }
        
        assertWasCalled("peliKaynnissa", "Et kutsu metodia hirsipuu.peliKaynnissa(). Lue tehtävänanto!");
    }

    @Test
    @Points("42.1")
    public void jatketaanKyselyaJosEiKirjoitettuLopeta() {
        MockInOut mio = new MockInOut("tekisi\nmieli\nlimpparia\nja\nsuklaata\nlopetanko\nlopettaisinko\nen!\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        boolean kysytaanKayttajalta = false;
        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
            kysytaanKayttajalta = true;
        }

        assertTrue("Peli loppuu vaikka käyttäjä ei ole kirjoittanut lopeta. Lopeta peli vasta kun käyttäjä syöttää lopeta-komennon.", kysytaanKayttajalta);
    }

    @Test
    @Points("42.1")
    public void lopetetaanKyselyKunSyotettyLopeta() {
        MockInOut mio = new MockInOut("ja\nsit\nkirjoitin\nlopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        boolean lopetetaanKyseleminen = true;
        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
            lopetetaanKyseleminen = false;
        }

        assertTrue("Peli tulee lopettaa kun käyttäjä kirjoittaa komennon lopeta.", lopetetaanKyseleminen);
    }

    @Test
    @Points("42.1")
    public void tarkistetaanPelinLoppumistaWhileTruessa() {
        MockInOut mio = new MockInOut("ja\nsit\nkirjoitin\nlopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertWasCalled("peliKaynnissa", "Tarkista onko peli käynnissä jokaisella kierroksella. Voit käyttää toistoehtoa while (hirsipuu.peliKaynnissa()) { ... ");
    }

    @Test
    @Points("42.2")
    public void tulostetaanTilanneKunKirjoitetaanTilanne() {
        MockInOut mio = new MockInOut("tilanne\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertWasCalled("tulostaTilanne", "Kun käyttäjä syöttää merkkijonon tilanne, sinun tulee tulostaa tilanne hirsipuu-pelin tulostaTilanne-metodilla.");
    }

    @Test
    @Points("42.2")
    public void eiTulostetaTilannettaKunEiKirjoitetaTilanne() {
        MockInOut mio = new MockInOut("jotain\nlopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertWasNotCalled("tulostaTilanne", "Älä tulosta tilannetta jos käyttäjä ei syötä tilanne-komentoa.");
    }

    @Test
    @Points("42.3")
    public void arvataanKunKirjain() {
        MockInOut mio = new MockInOut("a\ntilanne\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertWasCalled("arvaa", "Kun käyttäjä syöttää yhden kirjaimen, käytä hirsipuun arvaa-metodia arvauksen tekemiseen.");
    }

    @Test
    @Points("42.3")
    public void eiArvataKunEiKirjainta() {
        MockInOut mio = new MockInOut("jotain\nlopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertWasNotCalled("arvaa", "Jos käyttäjä ei syötä arvausta, eli yksittäistä kirjainta, älä tee arvausta hirsipuun arvaa-metodilla.");
    }

    @Test
    @Points("42.4")
    public void tulostetaanValikkoKunTyhjaa() {
        MockInOut mio = new MockInOut("\nlopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        int luku = Laskuri.getLuku();

        replace(method(HirsipuuKayttoliittyma.class, "tulostaValikko")).with(method(Laskuri.class, "kasvata"));

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertTrue("Kutsu ohjelmasi tulostaValikko-metodia kun käyttäjä syöttää tyhjän rivin. ", Laskuri.getLuku() == luku + 2);
    }

    @Test
    @Points("42.4")
    public void tulostetaanValikkoVainKerranKunEiTyhjaa() {
        MockInOut mio = new MockInOut("jeajea\nlopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        int luku = Laskuri.getLuku();

        replace(method(HirsipuuKayttoliittyma.class, "tulostaValikko")).with(method(Laskuri.class, "kasvata"));

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertTrue("Valikon tulostus tulee tapahtua vain ohjelman alussa jos käyttäjä ei erikseen syötä tyhjää riviä.", Laskuri.getLuku() == luku + 1);
    }

    @Test
    @Points("42.5")
    public void tulostetaanPelitilanneJokaisenKierroksenJalkeen() {
        MockInOut mio = new MockInOut("jeajea\nlopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertWasCalled("tulostaSana", "Toistolausekkeen lopussa tulee kutsua hirsipuun metodia tulostaSana.");
        assertWasCalled("tulostaUkko", "Toistolausekkeen lopussa tulee kutsua hirsipuun metodia tulostaUkko.");
    }

    @Test
    @Points("42.5")
    public void eiTulostetaPelitilannettaJosEiPaastaToistolausekkeenLoppuun() {
        MockInOut mio = new MockInOut("lopeta\n");
        ReflectionUtils.newInstanceOfClass(HirsipuuKayttoliittyma.class);

        try {
            HirsipuuKayttoliittyma.main(new String[0]);
        } catch (NoSuchElementException e) {
            // toistolausekkeessa useampia kyselyjä..
        }

        assertWasNotCalled("tulostaSana", "Metodia tulostaSana tulee kutsua vain toistolausekkeen lopussa. Älä kutsu sitä muualta.");
        assertWasNotCalled("tulostaUkko", "Metodia tulostaUkko tulee kutsua vain toistolausekkeen lopussa. Älä kutsu sitä muualta.");
    }

    private void assertWasCalled(String method, String msg) {
        List<String> mt = getMethodCalls();
        assertFalse(msg, mt == null || mt.isEmpty());
        assertTrue(msg, mt.contains(method));
    }

    private void assertWasNotCalled(String method, String msg) {
        List<String> mt = getMethodCalls();
        assertTrue(msg, mt == null || !mt.contains(method));
    }

    private List<String> getMethodCalls() {
        try {
            Method m = Hirsipuu.class.getDeclaredMethod("getCalledMethods");
            m.setAccessible(true);
            return (List<String>) m.invoke(null);
        } catch (Throwable e) {
        }
        return null;
    }
}
