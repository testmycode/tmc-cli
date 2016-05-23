package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.Scanner;

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
        } catch (ParseException | IOException e) {
            // todo: Logger
        }

        // todo: do something
        System.out.println(username + " : " + password);
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
