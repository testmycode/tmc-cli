package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.Application;
import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;
import fi.helsinki.cs.tmc.cli.io.TmcCliProgressObserver;
import fi.helsinki.cs.tmc.cli.tmcstuff.Settings;
import fi.helsinki.cs.tmc.cli.tmcstuff.SettingsIo;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "login", desc = "Login to TMC server")
public class LoginCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(LoginCommand.class);
    private Io io;

    @Override
    public void getOptions(Options options) {
        options.addOption("u", "user", true, "TMC username");
        options.addOption("p", "password", true, "Password for the user");
        options.addOption("s", "server", true, "Address for TMC server");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;
        String username = getLoginInfo(args, "u", "username: ");
        String password = getLoginInfo(args, "p", "password: ");
        String serverAddress = getLoginInfo(args, "s", "server address: ");

        Settings settings = new Settings(serverAddress, username, password);
        if (loginPossible(settings) && saveLoginSettings(settings)) {
            io.println("Login successful.");
        }
    }
    
    private String getLoginInfo(CommandLine line, String option, String prompt) {
        String info = line.getOptionValue(option);
        if (info == null) {
            info = io.readLine(prompt);
        }
        return info;
    }

    private boolean saveLoginSettings(Settings settings) {
        if (SettingsIo.save(settings)) {
            return true;
        } else {
            io.println("Login failed.");
            return false;
        }
    }

    /**
     * Try to contact TMC server. If successful, user exists.
     *
     * @return True if user exist
     */
    private boolean loginPossible(Settings settings) {
        Application app = getApp();
        app.createTmcCore(settings);
        TmcCore core = app.getTmcCore();
        Callable<List<Course>> callable = core.listCourses(
                new TmcCliProgressObserver(io));

        try {
            callable.call();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof FailedHttpResponseException) {
                FailedHttpResponseException httpEx
                        = (FailedHttpResponseException) cause;
                if (httpEx.getStatusCode() == 401) {
                    io.println("Incorrect username or password.");
                    return false;
                }
            }
            
            logger.error("Unable to connect to server", e);
            io.println("Unable to connect to server "
                    + settings.getServerAddress());
            return false;
        }

        return true;
    }
}
