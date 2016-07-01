Architecture and Hacking the tmc-cli
====================================

## Requirements for developing tmc-cli
 * JDK 7
 * Modern Linux Bash (Mac's Bash won't work)


## Building tmc-cli
To build tmc-cli run the appropriate Maven command.

	$ git clone https://github.com/tmc-cli/tmc-cli.git
	$ mvn clean install

## Architecture

Tmc-cli's program architecture is based on command design pattern. The program runs only a single command on each program execution.

Conceptually, there are three main parts in the program; the launch code, backend related code and commands. The commands are gathered into their own package. In addition, there are utility classes for user input/output and file handling.

Tmc-cli itself is mostly just an command line interface for the backend libraries. All the heavy lifting is done by [tmc-core](https://github.com/testmycode/tmc-core) and [tmc-langs](https://github.com/testmycode/tmc-langs) libraries. Tmc-cli also requires the server-side component of TestMyCode aka. [tmc-server](https://github.com/testmycode/tmc-server).

### Important classes

The `CliContext` object contains some cached data and singleton objects that are commonly used by utility classes and commands. Most importantly, it has the `Io` object which handles all user interaction via terminal. Never print anything using System.out.print(), since tests use the `TestIo` class which is dependent on the `Io` interface.

The `WorkDir` object handles most of the directory path handling. It is used by most commands to parse the exercise arguments.

The `TmcUtil` class is a static class with methods for interfacing with [tmc-core](https://github.com/testmycode/tmc-core/).

## Creating new commands

Every command must have `@Command` annotation and it's highly recommended to extend the AbstractCommand class. Note that the `@Command` annotation is used for creating help messages and its 'name' field is used as the sub-command name in terminal.

Please create all new commands inside the `command` package.

```java
@Command(name = "command-name", desc = "Command description goes here")
public class ExampleCommand extends AbstractCommand {
    @Override
    public void getOptions(Options options) {
        options.addOption("l", "long-option", false, "This will be seen in the help message.");
        // ...
    }
}
```

Document *all* commands and their options in MANUAL.md (read below for more on documentation).

## Logging error messages

In case of failure, please print debug info in the error log by using slf4j logger and print some useful error messages to user with `ctx.getIo().println(' ... ');`. Ctx context object is passed into most of the code in tmc-cli and you can use it to interact with the user.

## Unit testing

If you create a new command, please use integration tests only. If you want to verify that a command or a utility class has printed text into the terminal, use `io.assertContains()` method. This custom assert method prints easily understandable error messages when it fails and doesn't require much code.

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcUtil.class)
public class ExampleCommandTest {

    private Application app;
    private CliContext ctx;
    private TestIo io;
    private TmcCore mockCore;
    private WorkDir workDir;
    private Path tempDir;

    @Before
    public void setUp() {
        // redirect the course related files into tmp
        tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("exampleTest");
        workDir = new WorkDir(tempDir);

        io = new TestIo();
        mockCore = mock(TmcCore.class);// make sure that nothing is leaked to tmcCore
        ctx = new CliContext(io, workDir, mockCore);
        app = new Application(ctx);

        mockStatic(TmcUtil.class);
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (Exception e) { }
    }

    @Test
    public void exampleSucceedsWhenGivenValidCourse() {
        course = ...;
        when(TmcUtil.findCourse(eq(ctx), eq("course1"))).thenReturn(course);

        String[] args = {"example course1"};
        app.run(args);
        io.assertContains("something went right");
    }
}
```

If you are doing tests for any other class, simply create normal unit tests that don't depend on any command.

##Adding properties

Properties are saved as a Java `HashMap<String, String>`. They are read from ~/.config/tmc-cli/properties.json on initialisation. The purpose of the properties file is to provide a backwards- and forwards-compatible method of storing user preferences and internal data. Properties can be accessed via the `CliContext` class method getProperties(). Remember to store any changes to the properties with saveProperties(). Feel free to create new properties, but please document *all* properties in the 'COMMAND: PROP'-section of MANUAL.md.

##Updating the documentation

Please document any new features or revisions in MANUAL.md and HISTORY.md as well as README.md, if the affected feature is already documented there.

If you make changes to MANUAL.md, please rebuild tmc.1 with [md2man](https://github.com/sunaku/md2man) before you push your changes. Use `md2man-roff docs/MANUAL.md > docs/tmc.1` to build the manpage.

There are no strict guidelines for README.md or MANUAL.md, but please try not to deviate from the original style (eg. new command sections should follow the same pattern). 
