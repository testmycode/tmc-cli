package fi.helsinki.cs.tmc.cli.io;

public abstract class Io {

    public abstract void print(String str);

    public void print(Object obj) {
        print(String.valueOf(obj));
    }

    public void println(String str) {
        print(str + "\n");
    }

    public abstract String readLine(String prompt);

    public abstract String readPassword(String prompt);
}
