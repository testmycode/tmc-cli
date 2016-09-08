package fi.helsinki.cs.tmc.cli.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class ConfigCommandTest {

    private Application app;
    private TestIo io;
    private HashMap<String, String> props;

    @Before
    public void setup() {
        io = new TestIo();
        CliContext ctx = Mockito.spy(new CliContext(io));
        app = new Application(ctx);

        when(ctx.saveProperties()).thenReturn(true);
        props = new HashMap<>();
        when(ctx.getProperties()).thenReturn(props);
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
        app.run(new String[] {"config", "cool=yeah"});
        assertTrue(props.containsKey("cool"));
        assertTrue(props.containsValue("yeah"));
        io.assertContains(" cool set to \"yeah\".");
        io.assertAllPromptsUsed();
    }

    @Test
    public void setsOnePropertyQuietly() {
        app.run(new String[] {"config", "-q", "cool=yeah"});
        assertTrue(props.containsKey("cool"));
        assertTrue(props.containsValue("yeah"));
        io.assertNotContains(" cool set to \"yeah\".");
        io.assertAllPromptsUsed();
    }

    @Test
    public void setsOneExistingProperty() {
        io.addConfirmationPrompt(true);
        props.put("cool", "old");
        app.run(new String[] {"config", "cool=yeah"});
        assertTrue(props.containsKey("cool"));
        assertTrue(props.containsValue("yeah"));
        io.assertContains(" cool set to \"yeah\", it was \"old\".");
        io.assertAllPromptsUsed();
    }

    @Test
    public void setsMultiplePropertiesCorrectly() {
        io.addConfirmationPrompt(true);
        app.run(new String[] {"config", "cool=yeah", "hello=world"});
        assertEquals("yeah", props.get("cool"));
        assertEquals("world", props.get("hello"));
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
}
