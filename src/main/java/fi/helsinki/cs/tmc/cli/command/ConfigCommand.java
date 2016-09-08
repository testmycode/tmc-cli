package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@Command(name = "config", desc = "Set/unset TMC-CLI properties")
public class ConfigCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCommand.class);
    private CliContext context;
    private Io io;

    private HashMap<String, String> properties;
    private boolean quiet;

    @Override
    public String[] getUsages() {
        return new String[] {
            "[-q|--quiet] KEY=\"VALUE\"...",
            "-d|--delete [-q|--quiet] KEY...",
            "-l|--list",
            "-g|--get=KEY"};
    }

    @Override
    public void getOptions(Options options) {
        options.addOption("g", "get", true, "Get value of a key");
        options.addOption("d", "delete", false, "Unset given property keys");
        options.addOption("q", "quiet", false, "Don't ask confirmations");
        options.addOption("l", "list", false, "List all properties");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.context = context;
        this.io = context.getIo();

        boolean get = args.hasOption("g");
        boolean listing = args.hasOption("l");
        boolean delete = args.hasOption("d");
        this.quiet = args.hasOption("q");

        String[] arguments = args.getArgs();
        this.properties = context.getProperties();

        if ((get ? 1 : 0) + (listing ? 1 : 0) + (delete ? 1 : 0) > 1) {
            io.errorln("Only one of the --get or --list or --delete options can "
                    + "be used at same time.");
            printUsage(context);
            return;
        }

        if (listing) {
            if (arguments.length != 0) {
                io.errorln("Listing option doesn't take any arguments.");
                printUsage(context);
                return;
            }
            printAllProperties();
            return;
        }

        if(get) {
            if (arguments.length != 0) {
                io.errorln("There should not be extra arguments when using --get option.");
                printUsage(context);
                return;
            }
            String key = args.getOptionValue('g');
            boolean exists = properties.containsKey(key);
            if (!exists && !quiet) {
                io.errorln("The property " + key + " doesn't exist.");
                return;
            }
            io.println(exists ? properties.get(key) : "");
            return;
        } else if (delete) {
            deleteProperties(arguments);
        } else {
            setProperties(arguments);
        }
        context.saveProperties();
    }

    private void printAllProperties() {
        ArrayList<String> array = new ArrayList<>(properties.keySet());
        Collections.sort(array);

        for (String key : array) {
            io.println(key + "=" + properties.get(key));
        }
    }

    private void deleteProperties(String[] keys) {
        if (this.quiet) {
            for (String key : keys) {
                if (properties.containsKey(key)) {
                    properties.remove(key);
                }
            }
            return;
        }

        if (keys.length == 0) {
            io.errorln("Expected at least one property as argument.");
            printUsage(context);
            return;
        }

        for (String key : keys) {
            if (!properties.containsKey(key)) {
                io.error("Key " + key + " doesn't exist.");
                return;
            }
        }

        io.println("Deleting " + keys.length + " properties.");

        if (!io.readConfirmation("Are you sure?", true)) {
            return;
        }
        for (String key : keys) {
            String oldValue = properties.remove(key);
            io.println("Deleted key " + key + ", was " + oldValue);
        }
    }

    private void setProperties(String[] arguments) {
        if (arguments.length == 0) {
            io.errorln("Expected at least one key-value pair.");
            printUsage(context);
            return;
        }
        
        if (this.quiet) {
            setPropertiesQuietly(arguments);
            return;
        }

        io.print("Setting property keys:");
        for (String argument : arguments) {
            String[] parts = argument.split("=", 2);
            String oldValue = properties.get(parts[0]);
            io.print(" " + parts[0] + " set to \"" + parts[1] + "\"");

            if (oldValue != null) {
                io.println(", it was \"" + oldValue + "\".");
            } else {
                io.println(".");
            }
        }
        io.println();
        if (!io.readConfirmation("Are you sure?", true)) {
            return;
        }

        setPropertiesQuietly(arguments);
    }

    private void setPropertiesQuietly(String[] arguments) {
        for (String argument : arguments) {
            String[] parts = argument.split("=", 2);
            properties.put(parts[0], parts[1]);
        }
    }
}
