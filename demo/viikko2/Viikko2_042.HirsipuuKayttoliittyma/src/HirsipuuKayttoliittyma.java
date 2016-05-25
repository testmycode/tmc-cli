import hirsipuu.Hirsipuu;
import java.util.Scanner;

public class HirsipuuKayttoliittyma {
    public static void main(String[] args) {
        Scanner lukija = new Scanner(System.in);
        Hirsipuu hirsipuu = new Hirsipuu();

        System.out.println("************");
        System.out.println("* HIRSIPUU *");
        System.out.println("************");
        System.out.println("");
        tulostaValikko();
        System.out.println("");

        // OHJELMOI TOTEUTUKSESI TÄNNE


        System.out.println("Kiitos pelistä!");
    }

    public static void tulostaValikko() {
        System.out.println(" * valikko *");
        System.out.println("lopeta   - lopettaa pelin");
        System.out.println("tilanne  - tulostaa tarkemman tilanteen");
        System.out.println("yksittäinen kirjain arvaa annettua kirjainta");
        System.out.println("tyhjä rivi tulostaa tämän valikon");
    }
}
