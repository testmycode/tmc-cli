package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class ConfigCommandTest {

    private Application app;
    CliContext ctx;
    private TestIo io;
    private HashMap<String, String> props;
    private Settings settings;

    @Before
    public void setup() {
        io = new TestIo();
        ctx = Mockito.spy(new CliContext(io));
        app = new Application(ctx);

        when(ctx.saveProperties()).thenReturn(true);
        props = new HashMap<>();
        when(ctx.getProperties()).thenReturn(props);
        settings = new Settings();
        when(ctx.getSettings()).thenReturn(settings);
    }

    @Test
    public void printsErrorIfNoArgumentsGiven() {
        app.run(new String[] {"config"});
        io.assertContains("Expected at least one key-value pair.");
    }

    @Test
    public void printsErrorIfTwoConflictingOptionsGiven() {
        app.run(new String[] {"config", "--delete", "--list"});
        io.assertContains("Only one of the");
    }

    @Test
    public void printsErrorIfThreeConflictingOptionsGiven() {
        app.run(new String[] {"config", "--delete", "--list", "--get", "property"});
        io.assertContains("Only one of the");
    }

    @Test
    public void listsAllProperties() {
        props.put("hello", "world");
        props.put("toilet", "wonderland");
        app.run(new String[] {"config", "--list"});
        io.assertContains("hello=world");
        io.assertContains("toilet=wonderland");
    }

    @Test
    public void listsAllPropertiesWithExtraArgument() {
        props.put("hello", "world");
        props.put("toilet", "wonderland");
        app.run(new String[] {"config", "--list", "abc"});
        io.assertContains("Listing option doesn't take any arguments.");
    }

    @Test
    public void getProperty() {
        props.put("thing", "value");
        app.run(new String[] {"config", "--get", "thing"});
        io.assertContains("value");
        io.assertAllPromptsUsed();
    }

    @Test
    public void getUnexistingProperty() {
        app.run(new String[] {"config", "--get", "thing"});
        io.assertContains("The property thing doesn't exist");
        io.assertAllPromptsUsed();
    }

    @Test
    public void getUnexistingPropertyQuietly() {
        app.run(new String[] {"config", "--get", "-q", "thing"});
        io.assertNotContains("The property thing doesn't exist");
        io.assertAllPromptsUsed();
    }

    @Test
    public void getPropertyWithExtraArgument() {
        props.put("thing", "value");
        app.run(new String[] {"config", "--get", "thing", "abc"});
        io.assertContains("There should not be extra arguments when using --get option.");
    }

    @Test
    public void setsOnePropertyWhenNoOptionsGiven() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "testresults-right=red"});
        assertTrue(props.containsKey("testresults-right"));
        assertTrue(props.containsValue("red"));
        io.assertContains(" testresults-right is now \"red\".");
        io.assertAllPromptsUsed();
    }

    @Test
    public void setsOnePropertyQuietly() {
        app.run(new String[] {"config", "-q", "testresults-right=cyan"});
        assertTrue(props.containsKey("testresults-right"));
        assertTrue(props.containsValue("cyan"));
        io.assertNotContains(" testresults-right is now \"cyan\".");
        io.assertAllPromptsUsed();
    }

    @Test
    public void setsOneExistingProperty() {
        io.addConfirmationPrompt(true);
        props.put("testresults-right", "red");
        app.run(new String[] {"config", "testresults-right=blue"});
        assertTrue(props.containsKey("testresults-right"));
        assertTrue(props.containsValue("blue"));
        io.assertContains(" testresults-right is now \"blue\", was \"red\".");
        io.assertAllPromptsUsed();
    }

    @Test
    public void setsMultiplePropertiesCorrectly() {
        io.addConfirmationPrompt(true);
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "update-date=12345", "testresults-right=cyan"});
        assertEquals("12345", props.get("update-date"));
        assertEquals("cyan", props.get("testresults-right"));
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesZeroProperties() {
        app.run(new String[] {"config", "-d"});
        io.assertContains("Expected at least one property as argument.");
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesOneProperty() {
        io.addConfirmationPrompt(true);
        props.put("no", "e");
        app.run(new String[] {"config", "-d", "no"});
        assertTrue(!props.containsKey("no"));
        io.assertContains("Deleting 1 properties.");
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesOnePropertyQuietly() {
        props.put("no", "e");
        app.run(new String[] {"config", "-d", "-q", "no"});
        assertTrue(!props.containsKey("no"));
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesInvalidProperty() {
        app.run(new String[] {"config", "-d", "property"});
        assertTrue(!props.containsKey("no"));
        io.assertContains("Key property doesn't exist.");
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesOnePropertyWithNoConfirmation() {
        io.addConfirmationPrompt(false);
        props.put("no", "e");
        app.run(new String[] {"config", "-d", "no"});
        assertTrue(props.containsKey("no"));
        io.assertContains("Deleting 1 properties.");
        io.assertNotContains("Deleted key no, was");
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesMultiplePropertiesCorrectly() {
        io.addConfirmationPrompt(true);
        props.put("biggie", "smalls");
        props.put("snoop", "dogg");
        props.put("some", "thing");
        app.run(new String[] {"config", "-d", "biggie", "snoop"});
        assertTrue(!props.containsKey("biggie"));
        assertTrue(!props.containsKey("snoop"));
        assertTrue(props.containsKey("some"));
        assertEquals("thing", props.get("some"));
        io.assertAllPromptsUsed();
    }

    @Test
    public void onlyAllowedKeysAccepted() {
        app.run(new String[] {"config", "inValid=Argument"});
        io.assertContains("not an allowed key");
        io.assertContains("Allowed keys are:");
    }

    @Test
    public void sendDiagnosticsIsAllowed() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "send-diagnostics=true"});
        io.assertContains("Set send-diagnostics to");
    }

    @Test
    public void serverAddressIsAllowed() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "server-address=https://mooc.fi"});
        io.assertContains("Set server-address to");
    }

    @Test
    public void printsErrorWithTooFewArgumentsa() {
        app.run(new String[] {"config", "inValid"});
        io.assertContains("Expected at least one key-value pair.");
    }

    @Test
    public void serverAddressIsValidated() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "server-address=lol"});
        io.assertContains("Please start the address with http");
    }

    @Test
    public void httpsIsRequiredInBeginningInValidation() {
        app.run(new String[] {"config", "-q", "server-address=asdfhttps://"});
        io.assertContains("Please start the address with http");
    }

    @Test
    public void sendDiagnosticsIsValidated() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "send-diagnostics=lol"});
        io.assertContains("Please write either true or false");
    }

    @Test
    public void serverAddressConfiguredToSettings() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "server-address=https://mooc.fi"});
        assertEquals("https://mooc.fi", settings.getServerAddress());
    }

    @Test
    public void sendDiagnosticsConfiguredToSettings() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "send-diagnostics=true"});
        assertEquals(true, settings.getSendDiagnostics());
    }

    @Test
    public void configuredToSettingsWithQuiet() {
        app.run(new String[] {"config", "-q", "send-diagnostics=true"});
        assertEquals(true, settings.getSendDiagnostics());
    }

    @Test
    public void ifSeveralPairsAndSomeInvalidConfiguresValidOnes() {
        io.addConfirmationPrompt(true);
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "send-diagnostics=asdf", "server-address=https://mooc.fi"});
        io.assertContains("Please write either true or false");
        assertEquals("https://mooc.fi", settings.getServerAddress());
    }

    @Test
    public void ifSeveralPairsAndSomeInvalidValuesConfiguresValidOnesWithQuiet() {
        app.run(new String[] {"config", "-q",  "send-diagnostics=asdf", "server-address=https://mooc.fi"});
        io.assertContains("Please write either true or false");
        assertEquals("https://mooc.fi", settings.getServerAddress());
    }

    @Test
    public void ifSeveralPairsAndSomeInvalidKeysConfiguresValidOnes() {
        io.addConfirmationPrompt(true);
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config",  "asd=asdf", "server-address=https://mooc.fi"});
        io.assertContains("not an allowed key");
        assertEquals("https://mooc.fi", settings.getServerAddress());
    }

    @Test
    public void ifSeveralPairsAndSomeInvalidKeysConfiguresValidOnesWithQuiet() {
        app.run(new String[] {"config", "-q",  "asd=asdf", "server-address=https://mooc.fi"});
        io.assertContains("not an allowed key");
        assertEquals("https://mooc.fi", settings.getServerAddress());
    }
}
