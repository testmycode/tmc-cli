package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;

import java.io.IOException;
import java.util.Scanner;

public class LoginCommand implements Command {

    // todo: use our own terminal IO when available
    private final Scanner scanner;

    private Application app;

    public LoginCommand(Application app) {
        this.app = app;
        this.scanner = new Scanner(System.in);
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
            username = readLine("username: ");
            password = readPassword("password: ");
        } catch (Exception e) {
            // todo: Logger
        }

        //System.out.println(username + ":" + password);
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
