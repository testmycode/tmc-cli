package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Command(name = "prop", desc = "Set/unset TMC-CLI properties")
public class PropertiesCommand extends AbstractCommand {

    private static final Logger logger
            = LoggerFactory.getLogger(PropertiesCommand.class);
    private Io io;

    @Override
    public void getOptions(Options options) {
        options.addOption("u", "unset", false, "Unset given property keys");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;
        Boolean unset = args.hasOption("u");
        List<String> arguments = args.getArgList();
        HashMap<String, String> props = getApp().getProperties();
        if (arguments.isEmpty()) {
            printAllProps(props);
            return;
        }

        if (arguments.size() % 2 == 1 && !unset) {
            io.println("Invalid argument count. Usage: tmc prop KEY VALUE ...");
            return;
        }

        if (unset) {
            if (arguments.size() > 1) {
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
            getApp().saveProperties();
            return;
        } else {
            if (arguments.size() > 2) {
                io.print("Setting property keys:");
                for (int i = 0; i < arguments.size(); i = i + 2) {
                    io.print(" " + arguments.get(i) + "=>" + arguments.get(i + 1));
                }
                io.println("");
                if (!io.readConfirmation("Are you sure?", true)) {
                    return;
                }
            }
            for (int i = 0; i < arguments.size(); i = i + 2) {
                String last = props.put(arguments.get(i), arguments.get(i + 1));
                io.println("Set " + arguments.get(i) + "=>" + arguments.get(i + 1)
                        + ", was " + last);
            }
            getApp().saveProperties();
            return;
        }
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

        for (String key : props.keySet()) {
            sb = new StringBuilder();
            sb.append(key + ":");
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
