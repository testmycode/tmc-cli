package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.backend.SettingsIo;
import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import fi.helsinki.cs.tmc.cli.utils.BadValueTypeException;
import fi.helsinki.cs.tmc.cli.utils.BiConsumerWithException;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utilities.TmcServerAddressNormalizer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;

@Command(name = "config", desc = "Set/unset TMC-CLI properties")
public class ConfigCommand extends AbstractCommand {

    private CliContext context;
    private Io io;
    private static final Map<String, BiConsumerWithException<String, Object>> ALLOWED_KEYS = new HashMap<>();
    private static final Set<String> PROGRESS_BAR_COLORS = new HashSet<String>(Arrays.asList(new String[] {
            "black", "red", "green", "blue", "yellow", "blue", "purple", "cyan", "white", "none"
    }));

    private HashMap<String, String> properties;
    private boolean quiet;

    public ConfigCommand() {
        configureAllowedKeys();
    }

    private void configureAllowedKeys() {
        // add new possible config options here
        // each key has a BiConsumer function which validates the value and saves it in settings or properties
        ALLOWED_KEYS.put("send-diagnostics", (key, value) -> {
            String newVal = (String) value;
            if (!newVal.trim().toLowerCase().equals("true") && !newVal.trim().toLowerCase().equals("false")) {
                throw new BadValueTypeException("Please write either true or false");
            }
            boolean send = Boolean.parseBoolean(newVal);
            context.getSettings().setSendDiagnostics(send);
            SettingsIo.saveCurrentSettingsToAccountList(context.getSettings());
        });

        ALLOWED_KEYS.put("server-address", (key, address) -> {
            String addr = (String) address;
            if (!addr.matches("^https?://.*")) {
                throw new BadValueTypeException("Please start the address with http[s]://");
            }
            context.getSettings().setServerAddress(addr);
            if (!normalizeServerAddress()) {
                io.println("There was a problem setting the server address.");
                return;
            }
            SettingsIo.saveCurrentSettingsToAccountList(context.getSettings());
        });

        ALLOWED_KEYS.put("update-date", (key, value) -> {
            String date = (String) value;
            if (!date.matches("[0-9]+")) {
                throw new BadValueTypeException("Please insert the date as a number");
            }
            properties.put("update-date", date);
        });
        ALLOWED_KEYS.put("testresults-right", this::addToProperties);
        ALLOWED_KEYS.put("testresults-left", this::addToProperties);
        ALLOWED_KEYS.put("progressbar-left", this::addToProperties);
        ALLOWED_KEYS.put("progressbar-right", this::addToProperties);
    }

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
        arguments = Arrays.stream(arguments).filter(o -> !o.trim().isEmpty()).toArray(String[]::new);
        this.properties = context.getProperties();

        if (this.context.getSettings().getUsername().isPresent()) {
            this.context.getAnalyticsFacade().saveAnalytics(this.context.getSettings().getUsername().get(), "config");
        } else {
            this.context.getAnalyticsFacade().saveAnalytics("config");
        }

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

        if (get) {
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
        if (arguments.length == 0 || !arguments[0].contains("=")) {
            io.errorln("Expected at least one key-value pair.");
            printUsage(context);
            return;
        }
        
        if (this.quiet) {
            setPropertiesQuietly(arguments);
            return;
        }

        io.println("Setting property keys:");
        for (String argument : arguments) {
            String[] parts = argument.split("=", 2);
            if (!checkIfAllowedKey(parts[0])) {
                continue;
            }
            String oldValue = properties.get(parts[0]);
            if (io.readConfirmation(" Set " + parts[0] + " to \"" + parts[1] + "\"?", true)) {
                if (!saveValue(parts[0], parts[1])) {
                    continue;
                }
                io.print("Property " + parts[0] + " is now \"" + parts[1] + "\"");
                if (oldValue != null) {
                    io.println(", was \"" + oldValue + "\".");
                } else {
                    io.println(".");
                }
            }

        }
        io.println();
    }

    private void setPropertiesQuietly(String[] arguments) {
        for (String argument : arguments) {
            String[] parts = argument.split("=", 2);
            if (checkIfAllowedKey(parts[0])) {
                saveValue(parts[0], parts[1]);
            }
        }
    }

    private boolean saveValue(String key, String value) {
        try {
            ALLOWED_KEYS.get(key).apply(key, value);

        } catch (Exception e) {
            io.errorln(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean checkIfAllowedKey(String key) {
        if (!ALLOWED_KEYS.keySet().contains(key)) {
            io.println("\"" + key + "\" is not an allowed key.");
            io.println("Allowed keys are: ");
            ALLOWED_KEYS.keySet().stream().forEach(k -> io.print(" " + k + '\n'));
            return false;
        }
        return true;
    }

    private void addToProperties(String key, Object value) throws BadValueTypeException {
        String color = (String) value;
        if (!PROGRESS_BAR_COLORS.contains(color)) {
            throw new BadValueTypeException("Color " + value + " not supported.");
        }
        properties.put(key, color);
        SettingsIo.saveProperties(properties);
    }

    private boolean normalizeServerAddress() {
        TmcServerAddressNormalizer normalizer = new TmcServerAddressNormalizer();
        normalizer.normalize();
        try {
            this.context.getTmcCore().authenticate(ProgressObserver.NULL_OBSERVER, TmcSettingsHolder.get().getPassword().get()).call();
        } catch (Exception e) {
            return false;
        }
        normalizer.selectOrganizationAndCourse();
        return true;
    }
}
