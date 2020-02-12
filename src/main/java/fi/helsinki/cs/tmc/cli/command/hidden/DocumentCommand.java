package fi.helsinki.cs.tmc.cli.command.hidden;

import fi.helsinki.cs.tmc.cli.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.core.CliContext;
import fi.helsinki.cs.tmc.cli.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
import fi.helsinki.cs.tmc.cli.io.ColorUtil;
import fi.helsinki.cs.tmc.cli.io.EnvironmentUtil;
import fi.helsinki.cs.tmc.cli.io.Io;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Command(name = "document", desc = "Show lots of boring info about tmc-cli")
public class DocumentCommand extends AbstractCommand {

    private static final String ERROR_MESSAGE =
            ""
                    + "Exception in thread \"main\" java.lang.NullPointerException\n"
                    + "\tat fi.helsinki.cs.tmc.cli.command.EasterEggCommand.run("
                    + "DocumentCommand.java:78)\n"
                    + "\tat fi.helsinki.cs.tmc.cli.command.core.AbstractCommand.execute("
                    + "AbstractCommand.java:63)\n"
                    + "\tat fi.helsinki.cs.tmc.cli.Application.runCommand(Application.java:71)\n"
                    + "\tat fi.helsinki.cs.tmc.cli.Application.run(Application.java:129)\n"
                    + "\tat fi.helsinki.cs.tmc.cli.Application.main(Application.java:138)\n"
                    + "Exception in thread \"Thread-0\" java.lang.NullPointerException\n"
                    + "\tat fi.helsinki.cs.tmc.cli.io.ShutdownHandler.run(ShutdownHandler.java:18)";
    private Io io;
    private int width;
    private int height;
    private int cursorX;
    private int cursorY;
    private int speed;

    private Blittable logo;
    private Blittable logoText;

    private class Blittable {
        String[] lines;
        int width;

        public Blittable(String content) {
            if (content == null) {
                return;
            }
            this.lines = content.split("\n");
            for (String line : lines) {
                if (line.length() > width) {
                    width = line.length();
                }
            }
        }

        public boolean isLoaded() {
            return lines != null;
        }

        public String[] getContent() {
            return lines;
        }

        public int getHeight() {
            return lines.length;
        }

        public int getWidth() {
            return width;
        }
    }

    @Override
    public void getOptions(Options options) {
        options.addOption("h", true, "???");
        options.addOption("s", true, "???");
    }

    @Override
    public void run(CliContext context, CommandLine args) {
        this.io = context.getIo();
        this.width = EnvironmentUtil.getTerminalWidth();
        this.height = 30;
        this.cursorX = 0;
        this.cursorY = 0;
        this.speed = 100;

        if (args.hasOption('h')) {
            try {
                this.height = Integer.parseInt(args.getOptionValue('h'));
            } catch (NumberFormatException e) {
                io.errorln("Height must be integer");
                return;
            }
        }
        if (args.hasOption('s')) {
            try {
                this.speed = Integer.parseInt(args.getOptionValue('s'));
            } catch (NumberFormatException e) {
                io.errorln("Speed must be integer");
                return;
            }
        }
        if (EnvironmentUtil.isWindows()) {
            io.errorln("Command document doesn't exist. ;)");
            return;
        }

        io.println(ERROR_MESSAGE);
        cursorY += ERROR_MESSAGE.split("\n").length;
        wait(2000);

        for (; cursorY < height; cursorY++) {
            if (cursorY == height / 2) {
                io.println("Just kidding :)");
            }
            io.println();
            wait(50);
        }
        fadeOut();

        logo = new Blittable(loadFile("/logo.ansi"));
        logoText = new Blittable(loadFile("/text-logo.ansi"));
        if (!logo.isLoaded() || !logoText.isLoaded()) {
            io.println("Some demo resources couldn't be loaded.");
            return;
        }
        io.print("\u001B[?25l");

        scrollTheText();
        wait(3000);
        scrollTheDuck();

        int centerX = width / 3;
        int centerY = height / 2 - 5;

        setCursor(0, 0);
        io.print("\u001B[0J");

        blitter(
                ColorUtil.colorString("Original dev team", Color.BLUE),
                Math.max(centerX - 10, 0),
                centerY - 2);
        blitter("Johannes L. (jclc)", centerX, centerY++);
        centerY++;
        blitter("Juha V. (juvester)", centerX, centerY++);
        centerY++;
        blitter("Matti L. (matike)", centerX, centerY++);
        centerY++;
        blitter("Mikko M. (mikkomaa)", centerX, centerY++);
        centerY++;
        blitter("Aleksi S. (Salmela)", centerX, centerY++);
        wait(5000);

        setCursor(0, 0);
        io.print("\u001B[0J");

        centerY = height / 2 - 3;
        blitter(
                ColorUtil.colorString("Special thanks for", Color.CYAN),
                Math.max(centerX - 10, 0),
                centerY - 2);
        blitter("Jarmo Isotalo (Jamo)", centerX, centerY++);
        centerY++;
        blitter("Kati Kyllönen (kxkyll)", centerX, centerY++);
        centerY++;
        blitter("Matti Luukkainen (mluukkai)", centerX, centerY++);
        wait(4000);

        fadeOut();
        io.print("\u001B[1A\u001B[1A\u001B[0K");
        wait(200);
    }

    private void fadeOut() {
        io.print("\u001B[?25h");
        for (int i = height - 1; i >= 0; i--) {
            setCursor(0, i);
            io.print("\u001B[0K");
            wait(50);
        }
        io.print("\u001B[1A\u001B[0K");
        io.print("\u001B[1B");
    }

    private void scrollTheDuck() {
        int scrollY = -logo.getHeight();
        for (; scrollY < height; scrollY++) {
            blitter(logo, 10, scrollY);
            wait(50);
            for (int j = 0; j < logo.getHeight(); j++) {
                if (scrollY + j < 0 || scrollY + j >= height) {
                    continue;
                }
                setCursor(0, scrollY + j);
                io.print("\u001B[0K");
            }
        }
    }

    private void scrollTheText() {
        int scrollY = -logoText.getHeight();
        // scroll to the middle of screen
        int target = height / 2 - logoText.getHeight() / 2;

        while (true) {
            blitter(logoText, 10, scrollY);
            wait(50);

            scrollY++;
            if (scrollY >= target) {
                // don't erase the text after last rendering
                return;
            }

            for (int j = 0; j < logoText.getHeight(); j++) {
                if (scrollY + j < 0 || scrollY + j >= height) {
                    continue;
                }
                setCursor(0, scrollY + j);
                io.print("\u001B[0K");
            }
        }
    }

    private String loadFile(String filename) {
        InputStream stream = EnvironmentUtil.class.getResourceAsStream(filename);
        if (stream == null) {
            io.println("Loading failed :(");
            return null;
        }

        try {
            return IOUtils.toString(stream, StandardCharsets.UTF_8.toString());
        } catch (IOException e) {
            io.errorln("Encoding failure... REALLY???");
            return null;
        }
    }

    private void wait(int waitMs) {
        try {
            Thread.sleep(waitMs * speed / 100);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean setCursor(int posX, int posY) {
        if (posY < 0 || posY >= height) {
            return false;
        }
        int deltaY = posY - cursorY;
        if (deltaY < 0) {
            io.print("\u001B[" + (-deltaY) + "A");
        } else if (deltaY != 0) {
            io.print("\u001B[" + deltaY + "B");
        }
        io.print("\u001B[" + posX + "G");

        cursorX = posX;
        cursorY = posY;
        return true;
    }

    private void blitter(String content, int posX, int posY) {
        String[] lines = content.split("\n");
        blitter(lines, posX, posY);
    }

    private void blitter(Blittable blittable, int posX, int posY) {
        blitter(blittable.getContent(), posX, posY);
    }

    private void blitter(String[] lines, int posX, int posY) {
        for (int i = 0; i < lines.length; i++) {
            if (!setCursor(posX, posY + i)) {
                continue;
            }
            printLine(lines[i]);
        }
    }

    private void printLine(String line) {
        int length = line.length();
        int start = 0;
        int end = length;
        if (cursorX + line.length() >= width) {
            if (cursorX >= width) {
                return;
            }
            end = width - cursorX;
            length -= cursorX - width;
            //cursorX--;// the cursor will stick to the border
        }
        if (cursorX < 0) {
            if (cursorX < -line.length()) {
                return;
            }
            start = -cursorX;
        }
        io.print(trim(line, start, end));
    }

    private String trim(String line, int start, int end) {
        int index = 0;
        Integer actualStart = null;
        Integer actualEnd = null;
        for (int i = 0; i < line.length(); i++) {
            if (index == start && actualStart == null) {
                actualStart = i;
            }
            if (index == end) {
                actualEnd = i;
                break;
            }
            if (line.charAt(i) != '\u001B') {
                index++;
                continue;
            }
            i++;
            if (i >= line.length() || line.charAt(i) != '[') {
                throw new RuntimeException("Invalid ansi not supported");
            }
            for (i++; i < line.length(); i++) {
                char lineChar = line.charAt(i);
                if ((lineChar >= 'a' && lineChar <= 'z') || (lineChar >= 'A' && lineChar <= 'Z')) {
                    break;
                }
            }
            i++;
        }
        if (actualStart > 0 && actualEnd != null) {
            return line.substring(actualStart, actualEnd);
        }
        return line;
    }
}
