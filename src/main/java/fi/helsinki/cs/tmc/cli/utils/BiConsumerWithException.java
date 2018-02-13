package fi.helsinki.cs.tmc.cli.utils;

import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;

@FunctionalInterface
public interface BiConsumerWithException<R, T> {
    void apply(R r, T t) throws BadValueTypeException;
}
