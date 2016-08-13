package fi.helsinki.cs.tmc.cli.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.LinkedList;

/**
 * Used for verifying program outputs.
 */
public class TestIo extends Io {

    private enum PromptType {
        TEXT_PROMPT,
        PASSWORD_PROMPT,
        CONFIRM_PROMPT
    }

    private final StringBuilder printedText;
    private final LinkedList<PromptType> promptOrder;
    private final LinkedList<String> textPrompts;
    private final LinkedList<String> passwordPrompts;
    private final LinkedList<Boolean> confirmationPrompts;
    private int expectedPromptCount;

    public TestIo() {
        printedText = new StringBuilder();
        textPrompts = new LinkedList<>();
        promptOrder = new LinkedList<>();
        passwordPrompts = new LinkedList<>();
        confirmationPrompts = new LinkedList<>();
        expectedPromptCount = 0;
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

    public void assertAllPromptsUsed() {
        if (!allPromptsUsed()) {
            fail(
                    "Program should have created "
                            + expectedPromptCount
                            + " prompts instead of "
                            + promptOrder.size()
                            + " prompts.");
        }
    }

    public void addLinePrompt(String text) {
        addPrompt(PromptType.TEXT_PROMPT, text);
    }

    public void addPasswordPrompt(String text) {
        addPrompt(PromptType.PASSWORD_PROMPT, text);
    }

    public void addConfirmationPrompt(Boolean confirmation) {
        addPrompt(PromptType.CONFIRM_PROMPT, confirmation);
    }

    public void clearPrompts() {
        textPrompts.clear();
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
        usePrompt(PromptType.TEXT_PROMPT);
        this.printedText.append(prompt).append("\n");
        return textPrompts.pop();
    }

    @Override
    public String readPassword(String prompt) {
        usePrompt(PromptType.PASSWORD_PROMPT);
        this.printedText.append(prompt).append("\n");
        return passwordPrompts.pop();
    }

    @Override
    public boolean readConfirmation(String prompt, boolean defaultToYes) {
        usePrompt(PromptType.CONFIRM_PROMPT);
        String yesNo = (defaultToYes) ? " [Y/n] " : " [y/N] ";
        this.printedText.append(prompt).append(yesNo);
        return confirmationPrompts.pop();
    }

    private <T> void addPrompt(PromptType type, T value) {
        switch (type) {
            case TEXT_PROMPT:
                textPrompts.add((String) value);
                break;
            case PASSWORD_PROMPT:
                passwordPrompts.add((String) value);
                break;
            case CONFIRM_PROMPT:
                confirmationPrompts.add((Boolean) value);
                break;
            default:
                break;
        }
        promptOrder.add(type);
        expectedPromptCount++;
    }

    private void usePrompt(PromptType type) {
        if (promptOrder.isEmpty()) {
            throw new AssertionError("Too many prompts asked");
        }

        PromptType expected = promptOrder.pop();
        if (expected != type) {
            throw new AssertionError(
                    "Wrong prompt type expected " + expected.name() + ", got " + type);
        }
    }

    private boolean allPromptsUsed() {
        return textPrompts.isEmpty() && passwordPrompts.isEmpty() && confirmationPrompts.isEmpty();
    }
}
