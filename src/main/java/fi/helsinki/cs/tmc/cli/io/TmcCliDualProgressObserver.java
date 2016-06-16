package fi.helsinki.cs.tmc.cli.io;

public class TmcCliDualProgressObserver extends TmcCliProgressObserver {

    private int pips1;
    private int pips2;
    private Color.AnsiColor color1;
    private Color.AnsiColor color2;

    private int stepsDone;
    private int stepsTotal;

    public TmcCliDualProgressObserver(Io io, int steps) {
        this(io, steps, Color.AnsiColor.ANSI_CYAN, Color.AnsiColor.ANSI_CYAN);
    }

    public TmcCliDualProgressObserver(Io io, int steps,
                  Color.AnsiColor color1, Color.AnsiColor color2) {
        if (steps <= 0) {
            throw new IllegalArgumentException();
        }
        super.io = io;
        this.stepsTotal = steps;
        this.stepsDone = 0;
        this.color1 = color1;
        this.color2 = color2;
        super.maxline = super.getMaxline();
        this.pips1 = ((this.maxline / 3) * 2) - 12;
        this.pips2 = this.maxline - this.pips1 - 17;
    }

    @Override
    public void progress(long id, String message) {

    }

    @Override
    public void progress(long id, Double progress, String message) {
        if (message != null
                && (lastMessage == null || !lastMessage.equals(message))) {
            super.printMessage(message);
            lastMessage = message;
            io.println("");
        }
        io.print("\r"
                + stepsDoneString(stepsDone, stepsTotal)
                + progressBar((double) stepsDone / stepsTotal, pips1, color1)
                + " " + percentage(progress)
                + progressBar(progress, pips2, color2));
    }

    public void changeStepBy(int amount) {
        this.stepsDone = this.stepsDone + amount;
        if (this.stepsDone == this.stepsTotal) {
            progress(0, 1.0, lastMessage);
        }
    }

    private String stepsDoneString(int done, int total) {
        StringBuilder sb = new StringBuilder();
        if (done < 10) {
            sb.append("   ");
        } else if (done < 100) {
            sb.append("  ");
        } else if (done < 1000) {
            sb.append(" ");
        }
        sb.append(done + "/" + total);
        while (sb.length() < 9) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
