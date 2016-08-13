package fi.helsinki.cs.tmc.cli.io;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public abstract class Io extends Writer {

    public abstract void print(String str);

    public void print(Object obj) {
        print(String.valueOf(obj));
    }

    public void println(String str) {
        print(str + "\n");
    }

    public void println() {
        print("\n");
    }

    public abstract void error(String str);

    public void error(Object obj) {
        print(String.valueOf(obj));
    }

    public void errorln(String str) {
        print(str + "\n");
    }

    public abstract String readLine(String prompt);

    public abstract String readPassword(String prompt);

    public abstract boolean readConfirmation(String prompt, boolean defaultToYes);

    @Override
    public void write(char[] cbuf, int offset, int len) throws IOException {
        print(new String(Arrays.copyOfRange(cbuf, offset, offset + len)));
    }

    @Override
    public void flush() throws IOException {}

    @Override
    public void close() throws IOException {}
}
