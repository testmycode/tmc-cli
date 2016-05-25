
import fi.helsinki.cs.tmc.edutestutils.MockStdio;
import fi.helsinki.cs.tmc.edutestutils.Points;
import java.lang.reflect.Method;
import java.util.List;
import nhlstats.NHLStatistics;
import org.junit.*;
import static org.junit.Assert.*;

@Points("24")
public class F_KomentojenSarja1Test {

    @Rule
    public MockStdio io = new MockStdio();

    @Test
    public void testi() {
        testMode();
        String input = "joukkue\nPHI\nmaalit\npelaaja\nFilppula\nlopeta\n";
        io.setSysIn(input);
        NhlOsa2.main(null);
        input = input.replaceAll("\n", " ");
        String viesti =  "tarkasta että komentosarja \""+input+"\" toimii";
        
        assertCalled(0, "sortByPoints", viesti);
        assertCalled(1, "team PHI", viesti);
        assertCalled(2, "sortByGoals", viesti);       
        assertCalled(3, "top", viesti);  
        assertCalled(4, "search Filppula", viesti);   
    }
    private void assertCalled(int n, String method, String msg) {
        List<String> mt = getMethodCalls();
        assertTrue(msg, mt.size() > n);
        assertTrue(msg, mt.get(n).contains(method));
    }
    
    private List<String> getMethodCalls() {
        try {
            Method m = NHLStatistics.class.getDeclaredMethod("calledMethods");
            m.setAccessible(true);
            return (List<String>) m.invoke(null);
        } catch (Throwable e) {
        }
        return null;
    }

    private void testMode() {
        try {                        
            Method m = NHLStatistics.class.getDeclaredMethod("testMode");
            m.setAccessible(true);
            m.invoke(null);            
        } catch (Throwable e) {
        }
    }

}