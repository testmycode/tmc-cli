package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.cli.tmcstuff.TmcUtil;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class LoginCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(TmcUtil.class);

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

        // todo: clean this.
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
            logger.error("Unable to parse username or password.");
        }

        Settings settings = new Settings(serverAddress, username, password);

        if (loginPossible(settings)) {
            SettingsIo settingsIo = new SettingsIo();
            if (settingsIo.save(settings)) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Failed to write config file. "
                        + "Login failed.");
            }
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
            Throwable cause = e.getCause();
            if (cause instanceof FailedHttpResponseException) {
                FailedHttpResponseException httpEx
                        = (FailedHttpResponseException) cause;
                if (httpEx.getStatusCode() == 401) {
                    System.out.println("Incorrect username or password.");
                    return false;
                }
            }
            logger.error("Unable to connect to server", e);
            System.out.println("Unable to connect to server "
                    + settings.getServerAddress());
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
        logger.info("System.console not present, unable to read password "
                + "securely. Reading password in cleartext.");
        return this.readLine(prompt);
    }
}
