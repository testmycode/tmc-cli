package fi.helsinki.cs.tmc.cli.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.InputStream;
import java.util.Scanner;

public class TerminalIo extends Io {
    
    private static final Logger logger = LoggerFactory.getLogger(TerminalIo.class);
    private final Scanner scanner;

    public TerminalIo(InputStream stream) {
        Scanner newScanner = null;
        if (stream != null) {
            newScanner = new Scanner(stream);
        }
        scanner = newScanner;
    }

    @Override
    public void print(String str) {
        System.out.print(str);
    }

    @Override
    public String readLine(String prompt) {
        print(prompt);

        try {
            return scanner.nextLine();
        } catch (Exception e) {
            logger.warn("Line could not be read.", e);
            return null;
        }
    }

    @Override
    public String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            try {
                return new String(console.readPassword(prompt));
            } catch (Exception e) {
                logger.warn("Password could not be read.", e);
            }
        } else {
            logger.warn("Failed to read password due to System.console()");
        }
        println("Unable to read password securely. Reading password in cleartext.");
        println("Press Ctrl+C to abort");
        return readLine(prompt);
    }

    @Override
    public boolean readConfirmation(String prompt, boolean defaultToYes) {
        String yesNo = (defaultToYes) ? " [Y/n] " : " [y/N] ";
        String input = readLine(prompt + yesNo).toLowerCase();

        switch (input) {
            case "y": //fall through
            case "yes":
                return true;
            case "n": //fall through
            case "no":
                return false;
            default:
                return defaultToYes;
        }
    }
}
