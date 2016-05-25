
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

@Points("27")
public class YhdestaSataanTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi() {
        ReflectionUtils.newInstanceOfClass(YhdestaSataan.class);
        YhdestaSataan.main(new String[0]);
        String tulostus = io.getSysOut().trim();
        String[] rivit = tulostus.split("\\s+");
        if (rivit.length != 100) {
            fail("Tulostuksessa pitäisi olla 100 riviä.");
        }

        if (!"1".equals(rivit[0])) {
            fail("Ensimmäisellä rivillä pitäisi olla luku 1, nyt oli " + rivit[0]);
        }

        if (!"50".equals(rivit[49])) {
            fail("Viidennelläkymmenennellä rivillä pitäisi olla luku 50, nyt oli " + rivit[49]);
        }

        if (!"100".equals(rivit[rivit.length - 1])) {
            fail("Viimeisellä rivillä pitäisi olla luku 100, nyt oli " + rivit[rivit.length - 1]);
        }
    }
}
