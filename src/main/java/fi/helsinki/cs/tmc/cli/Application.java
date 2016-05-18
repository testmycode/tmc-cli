package fi.helsinki.cs.tmc.cli;

import fi.helsinki.cs.tmc.cli.command.Command;
import fi.helsinki.cs.tmc.cli.command.CommandMap;

public class Application {
    public static void main(String[] args) {
        String commandName = args[0];
        
        Command command;
        CommandMap map = new CommandMap();
        command = map.getCommand(commandName);
        if (command == null) {
            System.out.println("Command " + commandName + " doesn't exist.");
            System.exit(0);
        }
        command.run();
    }
}
