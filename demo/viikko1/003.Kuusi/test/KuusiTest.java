import org.junit.Test;
import org.junit.Rule;

import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;

@Points("3")
public class KuusiTest {

    @Rule
    public MockStdio io = new MockStdio();

    public String[] oikein =
    {"    *",
     "   ***",
     "  *****",
     " *******",
     "*********",
     "    *"};

    @Test
    public void test() {
        Kuusi.main(new String[0]);
        String out = io.getSysOut();
        assertTrue("Et tulostanut yhtään tähteä!",out.contains("*"));

        String[] lines = out.split("\n");

        assertEquals("Ohjelmasi pitäisi tulostaa 6 riviä, eli siinä pitäisi olla 6 System.out.println()-komentoa.",
                     oikein.length, lines.length);

        assertEquals("Kuusen ensimmäinen rivi on väärin",oikein[0],lines[0]);
        assertEquals("Kuusen toinen rivi on väärin",oikein[1],lines[1]);
        assertEquals("Kuusen kolmas rivi on väärin",oikein[2],lines[2]);
        assertEquals("Kuusen neljäs rivi on väärin",oikein[3],lines[3]);
        assertEquals("Kuusen viides rivi on väärin",oikein[4],lines[4]);
        assertEquals("Kuusen kuudes rivi on väärin",oikein[5],lines[5]);

    }

}