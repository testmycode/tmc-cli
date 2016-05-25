import org.junit.Test;
import org.junit.Rule;

import static org.junit.Assert.*;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.MockStdio;

@Points("2")
public class HeiMaailmaTest {

    @Rule
    public MockStdio io = new MockStdio();

    public String[] oikein =
    {"Hei Maailma!",
     "(Ja Mualima!)"};

    @Test
    public void test() {
        HeiMaailma.main(new String[0]);
        String out = io.getSysOut();
        assertTrue("Et tulostanut mitään!",out.length()>0);

        String[] lines = out.split("\n");

        assertEquals("Ohjelmasi pitäisi tulostaa 2 riviä, eli siinä pitäisi olla 2 System.out.println()-komentoa.",
                     oikein.length, lines.length);

        assertEquals("Ensimmäinen rivi on väärin",oikein[0],lines[0]);
        assertEquals("Toinen rivi on väärin",oikein[1],lines[1]);
    }

}