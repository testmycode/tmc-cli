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

@Command(name = "prop", desc = "Set/unset TMC-CLI properties")
public class PropertiesCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesCommand.class);
    private Io io;

    @Override
    public void getOptions(Options options) {
        options.addOption("u", "unset", false, "Unset given property keys");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.io = context.getIo();

        boolean unset = args.hasOption("u");
        String[] arguments = args.getArgs();
        HashMap<String, String> props = context.getProperties();
        if (arguments.length == 0) {
            printAllProps(props);
            return;
        }

        if (arguments.length % 2 == 1 && !unset) {
            io.println("Invalid argument count. Usage: tmc prop KEY VALUE ...");
            return;
        }

        if (unset) {
            if (arguments.length > 1) {
                io.print("Unsetting property keys:");
                for (String arg : arguments) {
                    io.print(" " + arg);
                }
                io.println("");
                if (!io.readConfirmation("Are you sure?", true)) {
                    return;
                }
            }
            for (String key : arguments) {
                io.println("Unset key " + key + ", was " + props.remove(key));
            }
        } else {
            if (arguments.length > 2) {
                io.print("Setting property keys:");
                for (int i = 0; i < arguments.length; i = i + 2) {
                    io.print(" " + arguments[i] + "=>" + arguments[i + 1]);
                }
                io.println("");
                if (!io.readConfirmation("Are you sure?", true)) {
                    return;
                }
            }
            for (int i = 0; i < arguments.length; i = i + 2) {
                String last = props.put(arguments[i], arguments[i + 1]);
                io.println("Set " + arguments[i] + "=>" + arguments[i + 1] + ", was " + last);
            }
        }
        context.saveProperties();
    }

    private void printAllProps(HashMap<String, String> props) {
        int longest = longestKey(props);
        io.println("TMC-CLI properties:");
        StringBuilder sb = new StringBuilder();
        sb.append("<KEY>  ");
        for (int i = 7; i < longest + 1; i++) {
            sb.append(" ");
        }
        sb.append("<VALUE>");
        io.println(sb.toString());

        ArrayList<String> array = new ArrayList<>(props.keySet());
        Collections.sort(array);

        for (String key : array) {
            sb = new StringBuilder();
            sb.append(key).append(":");
            for (int i = key.length() + 1; i < Math.max(longest + 1, 7); i++) {
                sb.append(" ");
            }
            sb.append(props.get(key));
            io.println(sb.toString());
        }
    }

    private int longestKey(HashMap<String, String> props) {
        int longest = 0;
        for (String key : props.keySet()) {
            longest = Math.max(longest, key.length());
        }
        return longest;
    }
}
