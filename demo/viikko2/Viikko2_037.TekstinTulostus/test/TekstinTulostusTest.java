import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import java.lang.reflect.Field;

@Points("37")
public class TekstinTulostusTest {
    
    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testaaEtteiKenttia() {
        Field[] fs = TekstinTulostus.class.getDeclaredFields();
        if (fs.length!=0) {
            fail("Luokassa TekstinTulostus on kenttä nimeltään "+fs[0].getName()+" poista se!");
        }
    }

    @Test 
    public void testaaTulostaaJotain() {
        TekstinTulostus.tulostaTeksti();
        assertFalse("Et tulostanut mitään!", io.getSysOut().isEmpty());
    }

    @Test
    public void testaaTulostaaOikein() {
        TekstinTulostus.tulostaTeksti();
        assertEquals("Et tulostanut oikeaa merkkijonoa!", "Alussa olivat suo, kuokka ja Java.", io.getSysOut().trim());
    }
}