package fi.helsinki.cs.tmc.cli.io;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

/**
 * Created by jclakkis on 27.5.2016.
 */
public class TmcCliProgressObserver extends ProgressObserver {
    protected static final char PIPCHAR = '#';
    protected static final char EMPTYCHAR = ' ';
    protected static final char BARLEFT = '[';
    protected static final char BARRIGHT = ']';

    protected Io io;
    private int pips;
    protected int maxline;
    private Color.AnsiColor color;
    protected String lastMessage;
    protected Boolean hasProgressBar;

    public TmcCliProgressObserver() {
        this(new TerminalIo());
    }

    public TmcCliProgressObserver(Io io) {
        this(io, Color.AnsiColor.ANSI_CYAN);
    }

    public TmcCliProgressObserver(Io io, Color.AnsiColor color) {
        this.hasProgressBar = false;
        this.io = io;
        this.maxline = getMaxline();
        this.pips = this.maxline - 6;
        this.color = color;
    }

    // Clearly this is not the best place for this function
    public static int getMaxline() {
        String colEnv = System.getenv("COLUMNS");
        if (colEnv != null && !colEnv.equals("")) {
            // Determine the terminal width - this won't work on Windows
            // Let's just hope our Windows users won't narrow their command prompt
            // We'll also enforce a minimum size of 20 columns

            return Math.max(Integer.parseInt(colEnv), 20);
        } else {
            return 70;
        }
    }

    @Override
    public void progress(long id, String message) {
        if (lastMessage == null || !lastMessage.equals(message)) {
            printMessage(message);
            lastMessage = message;
        }
    }

    @Override
    public void progress(long id, Double progress, String message) {
        this.hasProgressBar = true;
        if (lastMessage == null || !lastMessage.equals(message)) {
            printMessage(message);
            lastMessage = message;
            io.println("");
        }
        this.io.print("\r" + this.progressBar(progress, this.maxline, this.color));
    }

    protected void printMessage(String message) {
        message = shorten(message, maxline);
        io.print("\r" + message);
        flush(maxline - message.length());
    }

    @Override
    public void start(long id) {
    }

    @Override
    public void end(long id) {
        // Most likely not going to be used
        if (this.hasProgressBar) {
            this.io.println("\r" + this.percentage(1.0)
                    + this.progressBar(1.0, this.pips, this.color));
        } else {
            this.io.println("");
        }
    }

    protected String shorten(String str, int length) {
        if (str.length() <= length) {
            return str;
        } else {
            return str.substring(0, length - 3) + "...";
        }
    }

    protected void flush(int length) {
        // "Flush" the rest of the line if the next message is shorter than the last
        if (length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = length; i > 0; i--) {
            sb.append(' ');
        }
        io.print(sb);
    }

    public static String progressBar(double progress, int length, Color.AnsiColor color) {
        return progressBar(progress, length, color, color, BARLEFT, BARRIGHT, PIPCHAR, EMPTYCHAR);
    }

    public static String progressBar(
            double progress,
            int length,
            Color.AnsiColor color1,
            Color.AnsiColor color2,
            char barLeft,
            char barRight,
            char donePip,
            char notDonePip) {
        int pipsDone = (int) ((length - 6) * progress);
        StringBuilder sbLeft = new StringBuilder(pipsDone);
        StringBuilder sbRight = new StringBuilder((length - 6) - pipsDone);
        for (int i = 0; i < pipsDone; i++) {
            sbLeft.append(donePip);
        }
        for (int i = 0; i < (length - 6) - pipsDone; i++) {
            sbRight.append(notDonePip);
        }
        return percentage(progress)
                + barLeft
                + Color.colorString(sbLeft.toString(), color1)
                + Color.colorString(sbRight.toString(), color2)
                + barRight;
    }

    protected static String percentage(double progress) {
        int percent = (int) (progress * 100);
        String percentage;
        if (percent < 10) {
            percentage = "  ";
        } else if (percent < 100) {
            percentage = " ";
        } else {
            percentage = "";
        }
        return percentage + percent + "%";
    }
}
