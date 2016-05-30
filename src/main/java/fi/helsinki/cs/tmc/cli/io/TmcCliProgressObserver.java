package fi.helsinki.cs.tmc.cli.io;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

/**
 * Created by jclakkis on 27.5.2016.
 */
public class TmcCliProgressObserver extends ProgressObserver {
    private static final char PIPCHAR = '*';
    private static final char EMPTYCHAR = ' ';
    private static final char BARLEFT = '[';
    private static final char BARRIGHT = ']';
    private static final char MIDDLE_A = '-';
    private static final char MIDDLE_B = ' ';
    private final Io io;

    private int pips;
    private int maxline;

    public TmcCliProgressObserver() {
        this(new TerminalIo());
    }

    public TmcCliProgressObserver(Io io) {
        this.io = io;
        String colEnv = System.getenv("COLUMNS");
        if (colEnv != null) {
            // Determine the terminal width - this won't work on Windows
            // Let's just hope our Windows users won't narrow their command prompt
            // We'll also enforce a minimum size of 20 columns

            this.maxline = Math.max(Integer.parseInt(colEnv) - 1, 20);
        } else {
            this.maxline = 69;
        }
        this.pips = (this.maxline - 6) / 3;
    }

    @Override
    public void progress(long id, String message) {
        if (message.length() > this.maxline) {
            // Shorten message if necessary
            message = message.substring(0, this.maxline - 3) + "...";
        }
        this.io.print("\r" + message);
        flush(message.length());
    }

    @Override
    public void progress(long id, Double progress, String message) {
        if (message.length() > this.maxline - (this.pips + 6)) {
            // Shorten message if necessary
            message = message.substring(0, this.maxline - (this.pips + 6) - 3) + "...";
        }
        this.io.print("\r" + message
                + fillMiddle(this.maxline - (message.length() + this.pips + 6))
                + this.progressBar(progress) + this.percentage(progress));
    }

    @Override
    public void start(long id) {
    }

    @Override
    public void end(long id) {
        this.io.println("");
    }

    private String fillMiddle(int length) {
        // Fill the space between the message and the progress bar with lines
        if (length <= 0) {
            return "";
        } else if (length == 1) {
            return " ";
        } else if (length == 2) {
            return "  ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(' ');
        for (int i = length - 1; i > 0; i--) {
            if (i % 2 == 0) {
                sb.append(MIDDLE_A);
            } else {
                sb.append(MIDDLE_B);
            }
        }
        return sb.toString();
    }

    private void flush(int length) {
        // "Flush" the rest of the line if the next message is shorter than the last
        StringBuilder sb = new StringBuilder();
        for (int i = this.maxline - length; i > 0; i--) {
            sb.append(' ');
        }
        io.print(sb.toString());
    }

    private String progressBar(double progress) {
        int pipsDone = ((this.pips * (int) progress)) / 100;
        StringBuilder sb = new StringBuilder(this.pips);
        sb.append(BARLEFT);
        for (int i = 0; i < pipsDone; i++) {
            sb.append(PIPCHAR);
        }
        for (int i = 0; i < this.pips - pipsDone; i++) {
            sb.append(EMPTYCHAR);
        }
        sb.append(BARRIGHT);
        return sb.toString();
    }

    private String percentage(double progress) {
        int percent = (int) progress;
        String percentage = "";
        if (percent < 10) {
            percentage = "  ";
        } else if (percent < 100) {
            percentage = " ";
        }
        return percentage + percent + "%";
    }
}
