Hacking the tmc-cli
===================

## Requirements for developing tmc-cli
 * java jdk 7
 * linux bash (mac's bash won't work)


## Building a tmc-cli
To build tmc-cli run the appropriate maven command.

	$ git clone https://github.com/tmc-cli/tmc-cli.git
	$ mvn clean install

## Creating new commands

Every command must have `@Command` annotation and it's highly recomended to extend the
AbstractCommand. Note that the `@Command` annotation is used for creating help messages
and it's name field is used as the sub-command name in terminal.

Please put the new commands into the `command` package.

```java
@Command(name = "command-name", desc = "Description of command")
public class ExampleCommand extends AbstractCommand {
    @Override
    public void getOptions(Options options) {
        options.addOption("l", "long-option", false, "This will be seen in help message.");
        // ...
    }
}
```

## Architecture

The architecture is based around running single command per execution of program. The commands
use the utility classes in other packages. CliContext object contains some cached objects
that are commonly used in utility classes and commands. Most importantly it has the `Io` object
which responsibility is to print to the terminal, and help with other user interactions.

## Logging error messages

In case of failure please put some message into error log by using slf4j logger and print some
useful error message to user with `ctx.getIo().println(' ... ');`. Ctx context object is
passed into most of the code in the tmc-cli and you can use it to interact with the user.

## Unit testing

If you create new command then please use only integration tests. If you want to verify that command
or some utility class printed into terminal then use io.assertContains method. The custom assert
method gives good error messages when it fails and it's short to write.

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

If you are doing tests for any other class then just create normal unit tests
that don't depend on any command.
