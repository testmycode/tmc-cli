package fi.helsinki.cs.tmc.cli.io;

import java.util.LinkedList;

// This class is used to test program classes
public class TestIo extends Io {

    private StringBuilder printedText;
    private LinkedList<String> linePrompts;
    private LinkedList<String> passwordPrompts;

    public TestIo() {
        printedText = new StringBuilder();
        linePrompts = new LinkedList<String>();
        passwordPrompts = new LinkedList<String>();
    }

    public void addLinePrompt(String prompt) {
        linePrompts.add(prompt);
    }

    public void addPasswordPrompt(String prompt) {
        passwordPrompts.add(prompt);
    }

    public String getPrint() {
        return printedText.toString();
    }

    @Override
    public void print(String str) {
        printedText.append(str);
    }

    @Override
    public String readLine(String prompt) {
        return linePrompts.pop();
    }

    @Override
    public String readPassword(String prompt) {
        return passwordPrompts.pop();
    }
    
}
