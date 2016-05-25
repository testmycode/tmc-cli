import org.junit.Test;
import org.junit.Rule;

import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;

@Points("5")
public class SekuntejaVuodessaTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void test() {
        SekunnitVuodessa.main(new String[0]);
        String out = io.getSysOut();

        assertTrue("Et tulostanut mitään!",out.trim().length()>0);
        assertTrue("Tulostit väärän luvun!",out.contains("31536000"));
    }

}