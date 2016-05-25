
import java.util.Scanner;
import nhlstats.NHLStatistics;

public class NhlOsa2 {

    public static void main(String[] args) {
        Scanner lukija = new Scanner(System.in);

        while ( true ) {
            System.out.println("");
            System.out.print("komento (pisteet, maalit, syotot, jaahyt, pelaaja, joukkue, lopeta): ");
            String komento = lukija.nextLine();
            
            if ( komento.equals("lopeta")) {
                break;
            }
           
            if ( komento.equals("pisteet")) {
                // tulosta 10 parasta pisteiden tekijää
            } else if ( komento.equals("maalit")) {
                // tulosta 10 parasta maalintekijää     
            } else if ( komento.equals("syotot")) {
                // tulosta 10 parasta syöttäjää      
            } else if ( komento.equals("jaahyt")) {
                // tulosta 10 eniten jäähyjä saanutta    
            } else if ( komento.equals("pelaaja")) {
                // kysy käyttäjältä kenen tiedot halutaan tulostaa ja tulosta ne     
            } else if ( komento.equals("joukkue")) {
                // kysy käyttäjältä minkä joukkueen parhaat pisteiden tekijät halutaan tulostaa ja tulosta ne     
            }
        }        
    }
}

