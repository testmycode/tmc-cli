package fi.helsinki.cs.tmc.cli.io;

import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

/**
 * Created by jclakkis on 27.5.2016.
 */
public class TmcCliProgressObserver extends ProgressObserver {
    private static final int PIPS = 30;
    private static final int MAXLINE = 79;
    private static final char PIPCHAR = '*';
    private static final char EMPTYCHAR = ' ';
    private static final char BARLEFT = '[';
    private static final char BARRIGHT = ']';
    private static final char MIDDLE_A = '-';
    private static final char MIDDLE_B = ' ';
    private final Io io;

    public TmcCliProgressObserver() {
        this.io = new TerminalIo();
    }

    public TmcCliProgressObserver(Io io) {
        this.io = io;
    }

    @Override
    public void progress(long id, String message) {
        if (message.length() > MAXLINE) {
            // Shorten message if necessary
            message = message.substring(0, MAXLINE - 3) + "...";
        }
        this.io.print("\r" + message);
        flush(message.length());
    }

    @Override
    public void progress(long id, Double progress, String message) {
        if (message.length() > MAXLINE - (PIPS + 2)) {
            // Shorten message if necessary
            message = message.substring(0, MAXLINE - (PIPS + 2) - 3) + "...";
        }
        this.io.print("\r" + message
                + fillMiddle(MAXLINE - (message.length() + PIPS + 2))
                + this.progressBar(progress));
        //flush(message.length() + PIPS + 3);
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
        if (length == 0) {
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
        for (int i = MAXLINE - length; i > 0; i--) {
            sb.append(' ');
        }
        io.print(sb.toString());
    }

    private String progressBar(double progress) {
        int pipsDone = ((PIPS * (int) progress)) / 100;
        StringBuilder sb = new StringBuilder(PIPS);
        sb.append(BARLEFT);
        for (int i = 0; i < pipsDone; i++) {
            sb.append(PIPCHAR);
        }
        for (int i = 0; i < PIPS - pipsDone; i++) {
            sb.append(EMPTYCHAR);
        }
        sb.append(BARRIGHT);
        return sb.toString();
    }
}
