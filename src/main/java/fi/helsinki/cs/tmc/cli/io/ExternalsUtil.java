package fi.helsinki.cs.tmc.cli.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** Utility class for using external programs.
 */
public class ExternalsUtil {

    private static final Logger logger
            = LoggerFactory.getLogger(ExternalsUtil.class);

    /**
     * Create a temp file with a template and open an editor for the user.
     * Editor can be specified with the EDITOR env var.
     * The default editor is nano for Linux/Unix and notepad for Windows.
     *
     * @param template Line(s) to add to the end of the file
     * @param filename Descriptive name for the temp file - not the full path
     * @param escapeComments True if lines beginning with # should be ignored
     *
     * @return User created message
     */
    public static String getUserEditedMessage(
            String template, String filename, boolean escapeComments) {
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir")).resolve("tmc-cli")
                .resolve(filename + "-" + System.currentTimeMillis() + ".txt");
        if (!writeToFile(tempFile, template)) {
            return null;
        }
        String editor = System.getenv("EDITOR");
        if (editor == null) {
            if (EnvironmentUtil.isWindows()) {
                editor = "notepad";
            } else {
                editor = "nano";
            }
        }
        // User writes to file
        execExternalAndWait(editor, tempFile.toString());
        List<String> messageLines = readFromFileAsList(tempFile);
        if (messageLines == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String messageLine : messageLines) {
            if (messageLine.length() == 0
                    || (messageLine.charAt(0) != '#' || !escapeComments)) {
                sb.append(messageLine).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Show a string in a pager for the user, with colour.
     * Write output to a file and open it in pager
     * Assume colour has already been stripped on Windows.
     * @param string String to be shown to the user
     */
    public static void showStringInPager(String string, String filename) {
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir")).resolve("tmc-cli")
                .resolve(filename + "-" + System.currentTimeMillis() + ".txt");
        if (!writeToFile(tempFile, string)) {
            return;
        }
        showFileInPager(tempFile);
    }

    /**
     * Show a file in a pager for the user.
     * Use less in Linux/Unix and more in Windows.
     * Specify a custom pager with PAGER env var.
     * @param file File to be shown to the user (eg. a log file)
     */
    public static void showFileInPager(Path file) {
        String pager = System.getenv("PAGER");
        if (pager == null) {
            if (EnvironmentUtil.isWindows()) {
                pager = "more";
            } else {
                pager = "less -R";
            }
        }
        if (EnvironmentUtil.isWindows()) {
            execExternalAndWait("cmd.exe", "/c", pager, file.toString());
        } else {
            execExternalAndWait(pager, file.toString());
        }
    }

    public static boolean runUpdater(Io io, String pathToNewBinary) {
        if (!ExternalsUtil.execExternalAndWait("chmod", "u+x", pathToNewBinary)) {
            logger.error("Failed to set execution permissions to the new binary");
            io.println("Failed to set execution permissions to the new binary");
            return false;
        }
        if (!ExternalsUtil.execExternalAndWait(pathToNewBinary, "++internal-update")) {
            io.println("Failed to run the tmc-cli at " + pathToNewBinary);
            io.println("Run it with ++internal-update argument");
            logger.error("Failed to run the new tmc");
            return false;
        }
        return true;
    }

    /**
     * Open a URI in the default browser.
     * By default use the Java Desktop API.
     * If BROWSER env var is set, use that.
     * @param uri Link to be opened in browser
     */
    public static void openInBrowser(URI uri) {
        String browser = System.getenv("BROWSER");
        if (browser != null) {
            execExternal(browser, uri.toString());
        } else {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (Exception e) {
                    logger.error("Exception when launching browser", e);
                }
                return;
            }
            logger.error("Desktop is not supported, cannot launch browser");
        }
    }

    private static boolean execExternal(String... args) {
        return EnvironmentUtil.runProcess(args, false);
    }

    private static boolean execExternalAndWait(String... args) {
        return EnvironmentUtil.runProcess(args, true);
    }

    /**
     * Write something to a file. Warning: clobbers existing files.
     * @param path: Path to the file, use a path in temp
     * @param string: Text to write to the file
     * @return Whether or not writing was successful
     */
    private static boolean writeToFile(Path path, String string) {
        if (EnvironmentUtil.isWindows()) {
            string = string.replace("\n", "\r\n");
        }
        try {
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            PrintWriter writer = new PrintWriter(path.toFile());
            writer.print(string);
            writer.close();
        } catch (Exception e) {
            logger.error("Couldn't write to file", e);
            return false;
        }
        return true;
    }

    private static String readFromFileAsString(Path path) {
        String message;
        try {
            message = new String(Files.readAllBytes(path));
        } catch (Exception e) {
            logger.error("Couldn't read file as bytes", e);
            return null;
        }
        return message;
    }

    private static List<String> readFromFileAsList(Path path) {
        List<String> messageLines;
        try {
            messageLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Couldn't read file as array", e);
            return null;
        }
        return messageLines;
    }
}
