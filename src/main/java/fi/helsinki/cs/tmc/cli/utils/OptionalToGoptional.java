package fi.helsinki.cs.tmc.cli.utils;

import com.google.common.base.Optional;

public class OptionalToGoptional {

    public static <T> Optional<T> convert(java.util.Optional<T> convertable) {
        if (!convertable.isPresent()) {
            return  Optional.absent();
        }
        return Optional.of(convertable.get());
    }
}
