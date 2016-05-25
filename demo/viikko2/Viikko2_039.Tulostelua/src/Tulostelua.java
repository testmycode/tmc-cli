public class Tulostelua {

    public static void tulostaTahtia(int maara) {
        // 39.1
    }

    public static void tulostaNelio(int sivunpituus) {
        // 39.2
    }

    public static void tulostaSuorakulmio(int leveys, int korkeus) {
        // 39.3
    }

    public static void tulostaKolmio(int koko) {
        // 39.4
    }

    public static void main(String[] args) {

        // Testit eivät katso main-metodia, voit muutella tätä vapaasti.

        // HUOM: jos testit eivät meinaa mennä läpi, kokeile pääohjelmaa ajamalla,
        // että metodit toimivat niinkuin niiden on tarkoitus toimia!
        
        tulostaTahtia(3);
        System.out.println("\n---");  // tulostetaan kuvioiden välille ---
        tulostaNelio(4);
        System.out.println("\n---");
        tulostaSuorakulmio(5,6);
        System.out.println("\n---");
        tulostaKolmio(3);
        System.out.println("\n---");
    }
   
}