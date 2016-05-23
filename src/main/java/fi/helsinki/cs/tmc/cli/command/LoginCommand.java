package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class LoginCommand implements Command {

    // todo: use our own terminal IO when available
    private final Scanner scanner;

    private final GnuParser parser;
    private final Options options;

    private Application app;

    public LoginCommand(Application app) {
        this.app = app;
        this.scanner = new Scanner(System.in);
        this.parser = new GnuParser();
        this.options = new Options();
        options.addOption("u", "user", true, "TMC username");
        options.addOption("p", "password", true, "Password for the user");
        options.addOption("s", "server", true, "Address for TMC server");
    }

    @Override
    public String getDescription() {
        return "Login to TMC server.";
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public void run(String[] args) {
        String username = null;
        String password = null;
        String serverAddress = null;

        try {
            CommandLine line = this.parser.parse(options, args);

            username = line.getOptionValue("u");
            if (username == null) {
                username = readLine("username: ");
            }

            password = line.getOptionValue("p");
            if (password == null) {
                password = readPassword("password: ");
            }

            serverAddress = line.getOptionValue("s");
            if (serverAddress == null) {
                // todo: don't hardcode the default value, get it from somewhere
                serverAddress = "https://tmc.mooc.fi";
            }
        } catch (ParseException | IOException e) {
            // todo: Logger
        }

        Settings settings = new Settings(serverAddress, username, password);

        if (loginPossible(settings)) {
            // Todo. Save settings
            System.out.println("Login is succesfull!");
        } else {
            System.out.println("Login failed.");
        }
    }

    /**
     * Try to contact TMC server. If successful, user exists.
     *
     * @return True if user exist
     */
    private boolean loginPossible(Settings settings) {
        app.createTmcCore(settings);
        TmcCore core = this.app.getTmcCore();
        Callable<List<Course>> callable = core.listCourses(
                ProgressObserver.NULL_OBSERVER);

        try {
            callable.call();
        } catch (Exception e) {
            // todo: if 401, 404 do something
            return false;
        }

        return true;
    }

    // todo: use our own terminal IO when available
    private String readLine(String prompt) throws IOException {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    // todo: use our own terminal IO when available
    private String readPassword(String prompt) throws IOException {
        // Read the password in cleartext if no console is present (might happen
        // in some IDEs?)
        if (System.console() != null) {
            char[] pwd = System.console().readPassword(prompt);
            return new String(pwd);
        }
        return this.readLine(prompt);
    }
}
