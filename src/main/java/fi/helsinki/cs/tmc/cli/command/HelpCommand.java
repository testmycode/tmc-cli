package fi.helsinki.cs.tmc.cli.command;


public class HelpCommand implements Command {
    private CommandMap commands;

    public HelpCommand(CommandMap commandMap) {
        this.commands = commandMap;
    }

    @Override
    public String description() {
        return "Lists every command";
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public void run() {
        System.out.println("TMC commands:");
        for (Command command : this.commands.getCommands().values()) {
            System.out.println("  " + command.name() + "\t" + command.description());
        }
    }
}
