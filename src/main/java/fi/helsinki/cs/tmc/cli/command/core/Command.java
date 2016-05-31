package fi.helsinki.cs.tmc.cli.command.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * Name of the command.
     */
    String name() default "";
    /**
     * Command description.
     */
    String desc() default "";
}
