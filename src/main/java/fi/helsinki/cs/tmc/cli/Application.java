package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.Command;
import fi.helsinki.cs.tmc.cli.command.CommandMap;

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
}
