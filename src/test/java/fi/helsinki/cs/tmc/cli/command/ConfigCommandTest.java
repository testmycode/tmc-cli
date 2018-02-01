package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.analytics.AnalyticsFacade;
import fi.helsinki.cs.tmc.cli.backend.Account;
import fi.helsinki.cs.tmc.cli.backend.AccountList;
import fi.helsinki.cs.tmc.cli.backend.Settings;
import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import fi.helsinki.cs.tmc.cli.io.WorkDir;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;
import fi.helsinki.cs.tmc.langs.util.TaskExecutorImpl;
import fi.helsinki.cs.tmc.spyware.EventSendBuffer;
import fi.helsinki.cs.tmc.spyware.EventStore;
import fi.helsinki.cs.tmc.spyware.SpywareSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SettingsIo.class)
public class ConfigCommandTest {

    private Application app;
    CliContext ctx;
    private TestIo io;
    private TmcCore core;
    private AnalyticsFacade analyticsFacade;
    private HashMap<String, String> props;
    private Settings settings;
    private Path testConfigRoot;
    private static final String TEST_PROPERTIES_FILENAME = ".test-properties";

    @Before
    public void setup() throws IOException {
        io = new TestIo();
        settings = new Settings();
        TaskExecutor tmcLangs = new TaskExecutorImpl();
        core = new TmcCore(settings, tmcLangs);
        SpywareSettings analyticsSettings = new Settings();
        EventSendBuffer eventSendBuffer = new EventSendBuffer(analyticsSettings, new EventStore());
        analyticsFacade = new AnalyticsFacade(analyticsSettings, eventSendBuffer);
        ctx = Mockito.spy(new CliContext(io, core, new WorkDir(), new Settings(), analyticsFacade));
        app = new Application(ctx);

        when(ctx.saveProperties()).thenReturn(true);
        props = new HashMap<>();
        when(ctx.getProperties()).thenReturn(props);
        when(ctx.getSettings()).thenReturn(settings);
        testConfigRoot = Files.createTempDirectory(".test-config");
        mockStatic(SettingsIo.class);
        when(SettingsIo.getPropertiesFile(any(Path.class))).thenReturn(testConfigRoot.resolve(TEST_PROPERTIES_FILENAME));
        when(SettingsIo.getConfigDirectory()).thenReturn(testConfigRoot);
        AccountList t = new AccountList();
        t.addAccount(new Account("username", ""));
        when(SettingsIo.loadAccountList()).thenReturn(t);
        when(SettingsIo.saveAccountList(any(AccountList.class))).thenReturn(true);
    }

    @After
    public void cleanUp() {
        try {
            Files.deleteIfExists(testConfigRoot.resolve(TEST_PROPERTIES_FILENAME));
            Files.deleteIfExists(testConfigRoot);
        } catch (IOException e) {
        }
    }

    @Test
    public void doNotRunIfNotLoggedIn() {
        when(SettingsIo.loadAccountList()).thenReturn(new AccountList());
        app = new Application(ctx);

        String[] args = {"config -l"};
        app.run(args);
        io.assertContains("You are not logged in");
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
    public void listsOnlyPropsFromAllowedKeys() {
        props.put("hello", "world");
        props.put("toilet", "wonderland");
        app.run(new String[] {"config", "--list"});
        io.assertNotContains("hello=world");
        io.assertNotContains("toilet=wonderland");
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
        props.put("testresults-right", "red");
        app.run(new String[] {"config", "--get", "testresults-right"});
        io.assertContains("red");
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
        app.run(new String[] {"config", "testresults-left=blue", "testresults-right=cyan"});
        assertEquals("blue", props.get("testresults-left"));
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
        props.put("testresults-right", "cyan");
        app.run(new String[] {"config", "-d", "testresults-right"});
        assertTrue(!props.containsKey("cyan"));
        io.assertContains("Deleting 1 properties.");
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesOnePropertyQuietly() {
        props.put("testresults-right", "cyan");
        app.run(new String[] {"config", "-d", "-q", "testresults-right"});
        assertTrue(!props.containsKey("cyan"));
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesInvalidProperty() {
        app.run(new String[] {"config", "-d", "property"});
        assertTrue(!props.containsKey("no"));
        io.assertContains("Key property doesn't exist");
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesOnePropertyWithNoConfirmation() {
        io.addConfirmationPrompt(false);
        props.put("testresults-right", "cyan");
        app.run(new String[] {"config", "-d", "testresults-right"});
        assertTrue(props.containsKey("testresults-right"));
        io.assertContains("Deleting 1 properties.");
        io.assertNotContains("Deleted key no, was");
        io.assertAllPromptsUsed();
    }

    @Test
    public void deletesMultiplePropertiesCorrectly() {
        io.addConfirmationPrompt(true);
        props.put("testresults-right", "red");
        props.put("testresults-left", "green");
        props.put("progressbar-left", "purple");
        app.run(new String[] {"config", "-d", "testresults-right", "testresults-left"});
        assertTrue(!props.containsKey("testresults-left"));
        assertTrue(!props.containsKey("testresults-right"));
        assertTrue(props.containsKey("progressbar-left"));
        assertEquals("purple", props.get("progressbar-left"));
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

    @Test
    public void cannotDeleteAPropertyIfNotAnAllowedKey() {
        app.run(new String[] {"config", "-d", "last-submit"});
        io.assertContains("doesn't exist");
    }
}
