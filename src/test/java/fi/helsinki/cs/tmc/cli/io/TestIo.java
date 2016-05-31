package fi.helsinki.cs.tmc.cli.io;

// This class is used to test program classes
public class TestIo extends Io {
    
    public String printedText;

    public TestIo() {
        printedText = "";
    }

    @Override
    public void print(String str) {
        printedText += str;
    }

    @Override
    public String readLine(String prompt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String readPassword(String prompt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
