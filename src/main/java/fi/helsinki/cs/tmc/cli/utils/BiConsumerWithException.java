package fi.helsinki.cs.tmc.cli.utils;

@FunctionalInterface
public interface BiConsumerWithException<R, T> {
    void apply(R r, T t) throws BadValueTypeException;
}
