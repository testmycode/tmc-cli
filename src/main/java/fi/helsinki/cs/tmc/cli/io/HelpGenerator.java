package fi.helsinki.cs.tmc.cli.io;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;

public class HelpGenerator {
    public static void run(Io io, String usage, String description, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");

        String header = "\n" + description + "\u00A0\nOptions:";
        String footer = "\nCopyright(C) 2016 TestMyCode\nSome rights reserved.";
        formatter.printHelp(new PrintWriter(io), 80, usage, header, options, 2, 2, footer);
    }
}
