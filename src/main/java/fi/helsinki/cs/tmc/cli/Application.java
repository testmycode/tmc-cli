package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.Command;
import fi.helsinki.cs.tmc.cli.command.CommandMap;
import java.io.*;
import java.util.Properties;

public class Application {
    public static void main(String[] args) {
        String commandName;
        Command command;
        CommandMap map = new CommandMap();

        if(args.length == 0) {
            map.getCommand("help").run();
            return;
        }

        commandName = args[0];
        command = map.getCommand(commandName);
        if (command == null) {
            System.out.println("Command " + commandName + " doesn't exist.");
            System.exit(0);
        }
        command.run();
    }

    private static String getVersion() {

        String path = "/maven.prop";
        InputStream stream = Application.class.getResourceAsStream(path);
        if (stream == null)
            return "n/a";
        Properties props = new Properties();
        try {
            props.load(stream);
            stream.close();
            return (String) props.get("version");
        } catch (IOException e) {
            return "n/a";
        }
    }
}
