package fi.helsinki.cs.tmc.cli.io;

public abstract class Io {

    public abstract void print(String str);

    public abstract String readLine(String prompt);

    public abstract String readPassword(String prompt);

    public void println(String str) {
        print(str + "\n");
    }
}
