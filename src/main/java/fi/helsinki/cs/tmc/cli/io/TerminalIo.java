package fi.helsinki.cs.tmc.cli.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class TerminalIo extends Io {
    
    private static final Logger logger = LoggerFactory.getLogger(TerminalIo.class);

    @Override
    public void print(String str) {
        System.out.print(str);
    }

    @Override
    public String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return new Scanner(System.in).nextLine();
        } catch (Exception e) {
            logger.warn("Line could not be read.", e);
            return null;
        }
    }

    @Override
    public String readPassword(String prompt) {
        if (System.console() != null) {
            char[] pwd;
            try {
                pwd = System.console().readPassword(prompt);
            } catch (Exception e) {
                logger.warn("Password could not be read.", e);
                return null;
            }
            return new String(pwd);
        }

        // Read the password in cleartext if no console is present (might happen
        // in some IDEs?)
        logger.info("System.console is not present, unable to read password "
                + "securely. Reading password in cleartext.");
        return this.readLine("\n" + prompt);
    }

}
