package fi.helsinki.cs.tmc.cli.io;

public class ColorUtil {

    public static String colorString(String string, Color color) {
        if (!EnvironmentUtil.isWindows() && color != Color.NONE) {
            return color + string + Color.RESET;
        } else {
            return string;
        }
    }

    public static Color getColor(String name) {
        try {
            Color color = Color.valueOf(name.toUpperCase());
            if (color == Color.NONE || color == Color.RESET) {
                color = null;
            }
            return color;
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
