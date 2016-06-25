package fi.helsinki.cs.tmc.cli.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.LinkedList;

// This class is used to test program classes
public class TestIo extends Io {

    private final StringBuilder printedText;
    private final LinkedList<String> linePrompts;
    private final LinkedList<Boolean> confirmationPrompts;
    private final LinkedList<String> passwordPrompts;

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

    public void assertContains(String contains) {
        assertThat(out(), containsString(contains));
    }

    public void assertNotContains(String contains) {
        assertThat(out(), not(containsString(contains)));
    }

    public void assertEquals(String string) {
        assertThat(out(), is(string));
    }

    public void assertNotEquals(String string) {
        assertThat(out(), is(not(string)));
    }
}
