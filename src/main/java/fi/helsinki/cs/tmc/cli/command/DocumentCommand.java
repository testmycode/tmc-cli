package fi.helsinki.cs.tmc.cli.command;

import fi.helsinki.cs.tmc.cli.command.core.AbstractCommand;
import fi.helsinki.cs.tmc.cli.command.core.Command;
import fi.helsinki.cs.tmc.cli.io.Color;
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

    private Io io;
    private int width;
    private int height;
    private int cursorX;
    private int cursorY;

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
        options.addOption("i", false, "???");
        options.addOption("h", false, "???");
    }

    @Override
    public void run(CommandLine args, Io io) {
        this.io = io;
        this.width = EnvironmentUtil.getTerminalWidth();
        this.height = 30;
        cursorX = 0;
        cursorY = 0;

        if (args.hasOption('h')) {
            try {
                this.height = Integer.parseInt(args.getOptionValue('h'));
            } catch (Exception e) { }
        }
        if (EnvironmentUtil.isWindows()) {
            io.println("Command document doesn't exist. ;)");
            return;
        }
        if (args.hasOption('i')) {
            init();
        }

        for (; cursorY < height; cursorY++) {
            io.println("");
            wait(50);
        }
        fadeOut();

        //io.println("-----------------------------------------");
        //cursorY++;

        //Blittable logo = new Blittable(loadFile("/test.ansi"));
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

        blitter(Color.colorString("Dev team", Color.AnsiColor.ANSI_BLUE),
                Math.max(centerX - 10, 0), centerY - 2);
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

        centerY = height / 2 - 2;
        blitter("Jarmo Isotalo (Jamo)", centerX, centerY++);
        centerY++;
        blitter("Kati Kyllönen (kxkyll)", centerX, centerY++);
        wait(4000);

        fadeOut();
        io.print("\u001B[1A\u001B[0K");
        wait(50);
        io.print("\u001B[1A\u001B[0K");
        wait(200);
    }

    private void fadeOut() {
        io.print("\u001B[?25h");
        for (int i = height - 1; i > 0; i--) {
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
        int target = height / 2 - logoText.getHeight() / 2;

        while (true) {
            blitter(logoText, 10, scrollY);
            wait(50);

            scrollY++;
            if (scrollY >= target) {
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
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            io.println("Encoding failure... REALLY???");
            return null;
        }
    }

    private void wait(int waitMs) {
        try {
            Thread.sleep(waitMs);
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

    private void init() {
        String errorMessage = ""
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
        io.println(errorMessage);
        cursorY += errorMessage.split("\n").length;
        wait(10000);
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
                if ((lineChar >= 'a' && lineChar <= 'z')
                        || (lineChar >= 'A' && lineChar <= 'Z')) {
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