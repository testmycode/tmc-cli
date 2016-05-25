
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.util.Random;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Rule;
import org.junit.Test;

@Points("16")
public class ParitonVaiParillinenTest {

    @Rule
    public MockStdio io = new MockStdio();
     private Random r = new Random();

    @Test
    public void test() {
        testParitonVaiParillinen(1);
    }

    @Test
    public void testToinen() {
        testParitonVaiParillinen(0);
    }

    @Test
    public void testKolmas() {
        testParitonVaiParillinen(-1);
    }

    @Test
    public void lisaTest(){
            testParitonVaiParillinen(r.nextInt(40)-40);
    }
    
    @Test
    public void lisaTest1(){
            testParitonVaiParillinen(r.nextInt(40)-40);
    }
    
    @Test
    public void lisaTest2(){
            testParitonVaiParillinen(r.nextInt(40)-40);
    }
    
    @Test
    public void lisaTest3(){
            testParitonVaiParillinen(r.nextInt(40)-400);
    }
    
    @Test
    public void lisaTest4(){
            testParitonVaiParillinen(r.nextInt(40));
    }
    
    @Test
    public void lisaTest5(){
            testParitonVaiParillinen(r.nextInt(40)-400);
    }
    
    
            
    private void testParitonVaiParillinen(int luku) {
        ReflectionUtils.newInstanceOfClass(ParitonVaiParillinen.class);
        
        io.setSysIn(luku + "\n");
        ParitonVaiParillinen.main(new String[0]);

        String out = io.getSysOut();
      
        assertTrue("Et kysynyt käyttäjältä mitään!", out.trim().length() > 0);

        assertTrue("Kysymyksessäsi pitäisi olla merkki :, nyt ei ollut. Kysyit: " + out,
                out.contains(":"));

        if (luku % 2 == 0) {
            assertTrue("Tulosteessasi pitäisi olla teksti \"on parillinen\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("on parillinen"));
            assertFalse("Tulosteessasi ei saa olla tekstiä \"on pariton\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("on pariton"));
        } else {
            assertTrue("Tulosteessasi pitäisi olla teksti \"on pariton\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("on pariton"));
            assertFalse("Tulosteessasi ei saa olla tekstiä \"on parillinen\", kun annettu luku on " +
                    luku + ", nyt ei ollut. Tulosteesi oli: " + out,
                    out.contains("on parillinen"));
        }
        
    }
}
