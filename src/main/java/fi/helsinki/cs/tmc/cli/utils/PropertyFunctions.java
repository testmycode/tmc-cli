package fi.helsinki.cs.tmc.cli.utils;

/**
 * Created by nikkaire on 5.1.2018.
 */
public interface PropertyFunctions {

    String getter();

    void setter(String value) throws BadValueTypeException;
}
