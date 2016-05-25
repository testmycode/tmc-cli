
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.lang.reflect.Method;
import org.junit.*;
import static org.junit.Assert.*;

@Points("19")
public class IanTarkastusTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void sopivaIkaKelpaa() {
        int[] sopivat = {0, 1, 10, 85, 120};
        for (int ika : sopivat) {
            testaaJaTarkastaSopivaIka(ika);    
        }       
    }

    @Test
    public void sopimatonIkaEiKelpaa() {
        int[] sopivattomat = {-100, -1, 121, 1000};
        for (int ika : sopivattomat) {
            testaaJaTarkastaSopimatonIka(ika);    
        }       
    }    
    
    private void testaaJaTarkastaSopivaIka(int ika) {
        int oldOut = io.getSysOut().length(); 
        io.setSysIn(ika+"\n");
        callMain(IanTarkastus.class);
        String out = io.getSysOut().substring(oldOut);

        assertTrue("et tulosta mitään!", out.length() > 0);
        assertTrue("syötteelle "+ika+" pitäisi sanoa \"OK\", tulostit " + out, out.toLowerCase().contains("ok"));        
        assertTrue("syötteelle "+ika+" pitäisi sanoa \"OK\", tulostit " + out, !out.toLowerCase().contains("mah"));
    }
    
    private void testaaJaTarkastaSopimatonIka(int ika) {
        int oldOut = io.getSysOut().length(); 
        io.setSysIn(ika+"\n");
        callMain(IanTarkastus.class);
        String out = io.getSysOut().substring(oldOut);

        assertTrue("et tulosta mitään!", out.length() > 0);
        assertTrue("syötteelle "+ika+" pitäisi sanoa \"mahdotonta\", tulostit " + out, out.toLowerCase().contains("mah"));
        assertTrue("syötteelle "+ika+" pitäisi sanoa \"mahdotonta\", tulostit " + out, !out.toLowerCase().contains("ok"));
    }    

    private void callMain(Class kl) {
        try {
            kl = ReflectionUtils.newInstanceOfClass(kl);
            String[] t = null;
            String x[] = new String[0];
            Method m = ReflectionUtils.requireMethod(kl, "main", x.getClass());
            ReflectionUtils.invokeMethod(Void.TYPE, m, null, (Object) x);
        } catch (Throwable e) {
            fail(kl + "-luokan public static void main(String[] args) -metodi on hävinnyt!");
        }
    }
}
