package fi.helsinki.cs.tmc.cli.utils;

public interface PropertyFunctions {

    String getter();

    void setter(String value) throws BadValueTypeException;
}
