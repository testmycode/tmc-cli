package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.CliContext;
import fi.helsinki.cs.tmc.cli.io.TestIo;

import org.junit.Before;
import org.junit.Test;

public class DocumentCommandTest {

    private Application app;
    private CliContext ctx;
    private TestIo io;

    @Before
    public void setUp() {
        io = new TestIo();
        ctx = new CliContext(io);
        app = new Application(ctx);
    }

    @Test
    public void run() throws InterruptedException {
        app = new Application(ctx);

        String[] args = {"document", "-s", "0"};
        app.run(args);
        io.assertContains("dev team");
    }
}
