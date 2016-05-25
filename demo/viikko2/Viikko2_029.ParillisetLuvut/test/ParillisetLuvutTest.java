import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import org.junit.*;
import static org.junit.Assert.*;

@Points("29")
public class ParillisetLuvutTest {
    
    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi() {
        ReflectionUtils.newInstanceOfClass(ParillisetLuvut.class);
        ParillisetLuvut.main(new String[0]);
        String tulostus = io.getSysOut().trim();
        String[] rivit = tulostus.split("\\s+");
        if (rivit.length != 50) {
            fail("Tulostuksessa pitäisi olla 50 riviä.");
        }

        if (!"2".equals(rivit[0])) {
            fail("Ensimmäisellä rivillä pitäisi olla luku 2, nyt oli " + rivit[0]);
        }

        if (!"50".equals(rivit[24])) {
            fail("Rivin 25 pitäisi sisältää luku 50, nyt oli " + rivit[24]);
        }

        if (!"100".equals(rivit[rivit.length - 1])) {
            fail("Viimeisellä rivillä pitäisi olla luku 100, nyt oli " + rivit[rivit.length - 1]);
        }
    }
}
