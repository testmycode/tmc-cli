
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import fi.helsinki.cs.tmc.edutestutils.ReflectionUtils;
import java.lang.reflect.Method;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.Test;

@Points("17")
public class SuurempiLukuTest {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi1() {
        io.setSysIn("4\n5\n");
        callMain(SuurempiLuku.class);
        String out = io.getSysOut();

        assertTrue("Et tulostanut mitään!", out.trim().length() > 0);
        assertTrue("syöte 4 ja 5, tulostit " + out.trim(), out.trim().contains("5"));
        assertTrue("syöte 4 ja 5, tulostit " + out.trim(), !out.trim().contains("4"));

    }

    @Test
    public void testi2() throws Throwable {
        io.setSysIn("1\n-2\n");
        callMain(SuurempiLuku.class);
        String out = io.getSysOut();

        assertTrue("Et tulostanut mitään!", out.trim().length() > 0);
        assertTrue("syöte 1 ja -2, tulostit " + out.trim(), out.trim().contains("1"));
        assertTrue("syöte 1 ja -2, tulostit " + out.trim(), !out.trim().contains("-2"));
    }

    @Test
    public void testi3() {
        io.setSysIn("7\n7\n");
        callMain(SuurempiLuku.class);       
        String out = io.getSysOut();       

        assertTrue("Et tulostanut mitään!", out.trim().length() > 0);
        assertTrue("syöte 7 ja 7, tulostit " + out.trim(), !out.trim().contains("7"));
        assertTrue("kun syöte 7 ja 7, ohjelman pitäisi tulostaa \"Luvut ovat yhtä suuret\". Tulostit " , out.trim().toLowerCase().contains("yht"));
    }
    
    private void callMain(Class kl) {
        try {
            kl = ReflectionUtils.newInstanceOfClass(SuurempiLuku.class);
            String[] t = null;
            Method m = null;
            String x[] = new String[0];
            m = ReflectionUtils.requireMethod(kl, "main", x.getClass());
            ReflectionUtils.invokeMethod(Void.TYPE, m, null, (Object) x);
        } catch (Throwable e) {
            fail( kl+"-luokan public static void main(String[] args) -metodi on hävinnyt!");
        }
    }    
}