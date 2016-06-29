Hacking the tmc-cli
===================

## Requirements for developing tmc-cli
 * jdk 7
 * linux bash (mac's bash won't work)


## Building tmc-cli
To build tmc-cli run the appropriate Maven command.

	$ git clone https://github.com/tmc-cli/tmc-cli.git
	$ mvn clean install

## Creating new commands

Every command must have `@Command` annotation and it's highly recommended to extend the
AbstractCommand class. Note that the `@Command` annotation is used for creating help messages
and its 'name' field is used as the sub-command name in terminal.

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

## Architecture

Tmc-cli architecture is based on running only a single command on each program execution.
Commands use the utility classes in other packages. The `CliContext` object contains some cached
objects that are commonly used by utility classes and commands. Most importantly it has the `Io`
object which is responsible for printing messages for the user and helping with other user interactions.

## Logging error messages

In case of failure, please print debug info in the error log by using slf4j logger and print some
useful error messages to user with `ctx.getIo().println(' ... ');`. Ctx context object is
passed into most of the code in tmc-cli and you can use it to interact with the user.

## Unit testing

If you create a new command, please use integration tests only. If you want to verify that a command
or a utility class has printed text into the terminal, use `io.assertContains()` method. This custom
assert method prints easily understandable error messages when it fails and doesn't require much code.

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest(TmcUtil.class)
public class ExampleCommand {

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

If you are doing tests for any other class, simply create normal unit tests
that don't depend on any command.
