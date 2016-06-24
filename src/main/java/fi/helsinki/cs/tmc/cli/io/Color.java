package fi.helsinki.cs.tmc.cli.io;

public class Color {

    public enum AnsiColor {
        ANSI_NONE(""),
        ANSI_RESET("\u001B[0m"),
        ANSI_BLACK("\u001B[30m"),
        ANSI_RED("\u001B[31m"),
        ANSI_GREEN("\u001B[32m"),
        ANSI_YELLOW("\u001B[33m"),
        ANSI_BLUE("\u001B[34m"),
        ANSI_PURPLE("\u001B[35m"),
        ANSI_CYAN("\u001B[36m"),
        ANSI_WHITE("\u001B[37m");

        private String escCode;

        AnsiColor(String color) {
            this.escCode = color;
        }

        public String toString() {
            return this.escCode;
        }
    }

    public static String colorString(String string, AnsiColor color) {
        if (!EnvironmentUtil.isWindows() && color != AnsiColor.ANSI_NONE) {
            return color + string + AnsiColor.ANSI_RESET;
        } else {
            return string;
        }
    }

    public static AnsiColor getColor(String color) {
        if (color == null) {
            return null;
        }
        switch (color) {
            case "black":   return AnsiColor.ANSI_BLACK;
            case "red":   return AnsiColor.ANSI_RED;
            case "green":   return AnsiColor.ANSI_GREEN;
            case "yellow":   return AnsiColor.ANSI_YELLOW;
            case "blue":   return AnsiColor.ANSI_BLUE;
            case "purple":   return AnsiColor.ANSI_PURPLE;
            case "cyan":   return AnsiColor.ANSI_CYAN;
            case "white":   return AnsiColor.ANSI_WHITE;
            case "none":   return AnsiColor.ANSI_NONE;
            default:    return null;
        }
    }
}
