package fi.helsinki.cs.tmc.cli.command;


public class HelpCommand implements Command {
    private CommandMap commands;

    public HelpCommand(CommandMap commandMap) {
        this.commands = commandMap;
    }

    @Override
    public String getDescription() {
        return "Lists every command";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void run() {
        System.out.println("TMC commands:");
        for (Command command : this.commands.getCommands().values()) {
            System.out.println("  " + command.getName() + "\t" + command.getDescription());
        }
    }
}
