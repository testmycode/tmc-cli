package fi.helsinki.cs.tmc.cli.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(value = SourceVersion.RELEASE_6)
@SupportedAnnotationTypes(value = {"fi.helsinki.cs.tmc.cli.command.Command"})
public class CommandAnnotationProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandAnnotationProcessor.class);

    private Map<String, Class> commands = new HashMap<String, Class>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Executed");
        logger.info("Executed");
        for (Element elem : roundEnv.getElementsAnnotatedWith(Command.class)) {
            if (elem.getKind() == ElementKind.CLASS) {
                Command command = elem.getAnnotation(Command.class);
                commands.put(command.name(), elem.getClass());
                System.out.println(elem);
                System.out.println(elem.getClass().getCanonicalName());
            }
        }
        return true;
    }

    public Map<String, Class> getCommands() {
        return commands;
    }
}
