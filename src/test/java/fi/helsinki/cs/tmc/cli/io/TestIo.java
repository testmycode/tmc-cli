package fi.helsinki.cs.tmc.cli.io;

import java.util.LinkedList;

// This class is used to test program classes
public class TestIo extends Io {

    private StringBuilder printedText;
    private LinkedList<String> linePrompts;
    private LinkedList<Boolean> confirmationPrompts;
    private LinkedList<String> passwordPrompts;

    public TestIo() {
        printedText = new StringBuilder();
        linePrompts = new LinkedList<>();
        passwordPrompts = new LinkedList<>();
        confirmationPrompts = new LinkedList<>();
    }

    public boolean allPromptsUsed() {
        return linePrompts.isEmpty() && passwordPrompts.isEmpty()
                && confirmationPrompts.isEmpty();
    }

    public void addLinePrompt(String prompt) {
        linePrompts.add(prompt);
    }

    public void addPasswordPrompt(String prompt) {
        passwordPrompts.add(prompt);
    }

    public void addConfirmationPrompt(Boolean confirmation) {
        confirmationPrompts.add(confirmation);
    }

    public void clearPrompts() {
        linePrompts.clear();
        confirmationPrompts.clear();
        passwordPrompts.clear();
    }

    public String getPrint() {
        return printedText.toString();
    }

    public String out() {
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
    public Boolean readConfirmation(String prompt, Boolean defaultToYes) {
        if (confirmationPrompts.size() >= 1) {
            return confirmationPrompts.pop();
        } else {
            return defaultToYes;
        }
    }

    @Override
    public String readPassword(String prompt) {
        return passwordPrompts.pop();
    }

}
