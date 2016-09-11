package fi.helsinki.cs.tmc.cli.command.admin;

import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Io;

import fi.helsinki.cs.tmc.langs.LanguagePlugin;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.ExercisePackagingConfiguration;
import fi.helsinki.cs.tmc.langs.domain.Filer;
import fi.helsinki.cs.tmc.langs.domain.FilterFileTreeVisitor;
import fi.helsinki.cs.tmc.langs.domain.GeneralDirectorySkipper;
import fi.helsinki.cs.tmc.langs.domain.NoLanguagePluginFoundException;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.util.ProjectType;
import fi.helsinki.cs.tmc.langs.util.TaskExecutor;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*TODO split this command to multiple admin commands */
@Command(name = "admin", desc = "The admin command")
public class AdminCommand extends AbstractCommand {

    private static final Logger logger = LoggerFactory.getLogger(AdminCommand.class);
    private Io io;

    private TaskExecutor executor;
    private Path exercisePath;
    private Path outputPath;
    private Locale locale;

    @Override
    public void getOptions(Options options) {
        options.addOption("e", "exercise", true, "Path to exercise");
        options.addOption("o", "output", true, "Output path for the sub-command");
        options.addOption("l", "locale", true, "Locale of the exercise?");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.io = context.getIo();
        String[] stringArguments = args.getArgs();

        if (stringArguments.length == 0) {
            io.errorln("You have to give some sub-command.");
            printUsage(context);
            return;
        }

        if (!context.loadBackendWithoutLogin()) {
            return;
        }

        this.executor = context.getTmcLangs();
        String subCommand = stringArguments[0];
        if (args.hasOption("exercise")) {
            this.exercisePath = Paths.get(args.getOptionValue("exercise"));
        }
        if (args.hasOption("output")) {
            this.outputPath = Paths.get(args.getOptionValue("output"));
        }
        if (args.hasOption("locale")) {
            this.locale = new Locale(args.getOptionValue("locale"));
        }
        runSubCommand(subCommand);
    }

    private void runSubCommand(String subCommand) {
        switch (subCommand) {
            case "help":
                io.errorln("Not implemented!");
                break;
            case "checkstyle":
                runCheckCodeStyle();
                break;
            case "scan-exercise":
                runScanExercise();
                break;
            case "find-exercises":
                runFindExercises();
                break;
            case "run-tests":
                runTests();
                break;
            case "prepare-stubs":
                runPrepareStubs();
                break;
            case "prepare-solutions":
                runPrepareSolutions();
                break;
            case "get-exercise-packaging-configuration":
                runGetExercisePackagingConfiguration();
                break;
            case "clean":
                runClean();
                break;
            default:
                break;
        }
    }

    private void runCheckCodeStyle() {
        if (this.exercisePath == null || this.outputPath == null || this.locale == null) {
            io.errorln("Checkstyle command expects exercise path, output path and locale.");
            return;
        }
        ValidationResult validationResult;
        try {
            validationResult =
                    executor.runCheckCodeStyle(this.exercisePath, this.locale);
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", this.exercisePath, e);
            io.errorln(
                    "ERROR: Could not find suitable language plugin for the given exercise "
                            + "path.");
            return;
        }

        try {
            writeObjectIntoJsonFormat(validationResult, this.outputPath);
            io.println("Codestyle report can be found at " + this.outputPath);
        } catch (IOException e) {
            logger.error("Could not write result into {}", this.outputPath, e);
            io.errorln("ERROR: Could not write the results to the given file.");
        }
    }

    private void runScanExercise() {
        if (this.exercisePath == null || this.outputPath == null) {
            io.errorln("ScanExercise command expects exercise path and output path.");
            return;
        }
        String exerciseName = this.exercisePath.toFile().getName();
        Optional<ExerciseDesc> exerciseDesc;
        try {
            exerciseDesc = executor.scanExercise(this.exercisePath, exerciseName);

            if (exerciseDesc == null || !exerciseDesc.isPresent()) {
                logger.error("Absent exercise description after running scanExercise");
                io.errorln("ERROR: Could not scan the exercises.");
                return;
            }
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", this.exercisePath, e);
            io.errorln(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
            return;
        }

        try {
            writeObjectIntoJsonFormat(exerciseDesc.get(), this.outputPath);
            io.println(
                    "Exercises scanned successfully, results can be found in "
                            + this.outputPath);
        } catch (IOException e) {
            logger.error("Could not write output to {}", this.outputPath, e);
            io.errorln("ERROR: Could not write the results to the given file.");
        }
    }

    private void runFindExercises() {
        if (this.exercisePath == null || this.outputPath == null) {
            io.errorln("FindExercises command expects exercise path and output path.");
            return;
        }
        Path clonePath = this.exercisePath;
        final Set<String> exercises = new HashSet<>();
        Filer exerciseMatchingFiler =
                new Filer() {

                    @Override
                    public FileVisitResult decideOnDirectory(Path directory) {
                        if (executor.isExerciseRootDirectory(directory)) {
                            exercises.add(directory.toString());
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public void visitFile(Path source, Path relativePath) {}
                };
        new FilterFileTreeVisitor()
                .addSkipper(new GeneralDirectorySkipper())
                .setClonePath(clonePath)
                .setStartPath(clonePath)
                .setFiler(exerciseMatchingFiler)
                .traverse();

        try {
            writeObjectIntoJsonFormat(exercises, this.outputPath);
            io.println("Results can be found in " + this.outputPath);
        } catch (IOException e) {
            logger.error("Could not write output to {}", this.outputPath, e);
            io.errorln("ERROR: Could not write the results to the given file.");
        }
    }

    private void runTests() {
        if (this.exercisePath == null || this.outputPath == null) {
            io.errorln("RunTests command expects exercise path and output path.");
            return;
        }
        RunResult runResult;
        try {
            runResult = executor.runTests(this.exercisePath);
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", this.exercisePath, e);
            io.errorln(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
            return;
        }

        try {
            writeObjectIntoJsonFormat(runResult, this.outputPath);
            io.println("Test results can be found in " + this.outputPath);
        } catch (IOException e) {
            logger.error("Could not write output to {}", this.outputPath, e);
            io.errorln("ERROR: Could not write the results to the given file.");
        }
    }

    private void runPrepareStubs() {
        if (this.exercisePath == null || this.outputPath == null || this.locale == null) {
            io.errorln("RunTests command expects exercise path, output path and locale.");
            return;
        }
        try {
            executor.prepareStubs(
                    findExerciseDirectoriesAndGetLanguagePlugins(),
                    this.exercisePath,
                    this.outputPath);
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", this.exercisePath, e);
            io.errorln(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private void runClean() {
        try {
            executor.clean(this.exercisePath);
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", this.exercisePath, e);
            io.errorln(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private void runPrepareSolutions() {
        try {
            executor.prepareSolutions(
                    findExerciseDirectoriesAndGetLanguagePlugins(),
                    this.exercisePath,
                    this.outputPath);
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", this.exercisePath, e);
            io.errorln(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
        }
    }

    private void runGetExercisePackagingConfiguration() {
        ExercisePackagingConfiguration configuration;
        try {
            configuration = executor.getExercisePackagingConfiguration(this.exercisePath);
        } catch (NoLanguagePluginFoundException e) {
            logger.error(
                    "No suitable language plugin for project at {}", this.exercisePath, e);
            io.errorln(
                    "ERROR: Could not find suitable language plugin for the given "
                            + "exercise path.");
            return;
        }

        try {
            writeObjectIntoJsonFormat(configuration, this.outputPath);
            io.println("Results can be found in " + this.outputPath);
        } catch (IOException e) {
            logger.error("Could not write output to {}", this.outputPath, e);
            io.errorln("ERROR: Could not write the results to the given file.");
        }
    }

    private Map<Path, LanguagePlugin> findExerciseDirectoriesAndGetLanguagePlugins() {
        final Map<Path, LanguagePlugin> map = new HashMap<>();
        Filer exerciseMatchingFiler =
                new Filer() {

                    @Override
                    public FileVisitResult decideOnDirectory(Path directory) {
                        if (executor.isExerciseRootDirectory(directory)) {
                            try {
                                map.put(
                                        directory,
                                        ProjectType.getProjectType(directory).getLanguagePlugin());
                            } catch (NoLanguagePluginFoundException ex) {
                                throw new IllegalStateException(ex);
                            }
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public void visitFile(Path source, Path relativePath) {}
                };
        new FilterFileTreeVisitor()
                .addSkipper(new GeneralDirectorySkipper())
                .setClonePath(this.exercisePath)
                .setStartPath(this.exercisePath)
                .setFiler(exerciseMatchingFiler)
                .traverse();
        return map;
    }

    private static void writeObjectIntoJsonFormat(Object obj, Path outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile.toAbsolutePath().toFile())) {
            writer.write(new Gson().toJson(obj));
        }
    }
}