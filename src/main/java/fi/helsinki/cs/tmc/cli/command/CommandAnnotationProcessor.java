package fi.helsinki.cs.tmc.cli.command;

import java.util.ArrayList;
import java.util.List;
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

    private ArrayList<Class> commands = new ArrayList<Class>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Executed");
        for (Element elem : roundEnv.getElementsAnnotatedWith(Command.class)) {
            if (elem.getKind() == ElementKind.CLASS) {
                commands.add(elem.getClass());
                System.out.println(elem);
                System.out.println(elem.getClass().getCanonicalName());
            }
        }
        return false;
    }

    public List<Class> getCommands() {
        return commands;
    }
}
