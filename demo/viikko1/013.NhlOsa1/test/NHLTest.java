import nhlstats.NHLStatistics;
import fi.helsinki.cs.tmc.edutestutils.Points;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

@Points("13")
public class NHLTest {

    @Test
    public void nhlOsa1() throws Exception {
        testMode();
        Paaohjelma.main(null);
        assertCalled(0, "sortByGoals", "aloita järjestämällä pelaajat tehtyjen maalien suhteen, eli\n anna mainin alussa komento NHLStatistics.sortByGoals();");
        assertCalled(1, "top", "järjestetyäsi pelaajat maalien mukaan, tulosta pelaajat top-komennolla");
        assertCalled(2, "sortByPenalties", "tulostettuasi maalimäärän mukaan järjestetyt pelaajat, "
                + "järjestä pelaajat jäähyjen suhteen");
        assertCalled(3, "top", "tulosta nyt pelaajat top-komennolla");
        assertCalled(4, "search", "tulostettuasi jäähyjen mukaan järjestetyt pelaajat, etsi pelaajan Sidney Crosby tilastoja");
        assertCalled(5, "team PHI", "tulosta joukkueen PHI tilastot");
        assertCalled(6, "sortByPoints", "tulostettuasi joukkueen PHI pelaajatm järjestä pelaajat pisteiden suhteen jotta saat joukkueen ANA pelaajat oikeaan järjestykseen");
        assertCalled(7, "team ANA", "lopuksi tulosta joukkueen ANA tilastot");
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
            throw new Error("Jotain meni pieleen!",e);
        }
    }
}
