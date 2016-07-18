package fi.helsinki.cs.tmc.cli.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
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
