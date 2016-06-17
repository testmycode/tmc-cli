Hacking the tmc-cli
===================

## Requirements for developing tmc-cli
 * java jdk 7
 * linux bash (mac's bash won't work)


## Building a tmc-cli
To build tmc-cli run the appropriate maven command.

	$ git clone https://github.com/tmc-cli/tmc-cli.git
	$ mvn clean install

## Creating new commands

Every command must have `@Command` annotation and it's highly recomended to extend the AbstractCommand. Note that the `@Command` annotation is used for creating help messages and it's name field is used as the sub-command name in terminal.

Please put the new commands into the `command` package.

```java
@Command(name = "command-name", desc = "Description of command")
public class ExampleCommand extends AbstractCommand {
    @Override
    public void getOptions(Options options) {
        options.addOption("l", "long-option", false, "This will be seen in help message.");
        // ...
    }
}
```

## Architecture
...
