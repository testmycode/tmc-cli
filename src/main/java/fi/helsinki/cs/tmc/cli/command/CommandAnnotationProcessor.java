package fi.helsinki.cs.tmc.cli.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"fi.helsinki.cs.tmc.cli.command.Command", "java.lang.Override"})
public class CommandAnnotationProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandAnnotationProcessor.class);

    private static final String CLASS_NAME = "CommandList";
    private static final String PACKAGE_NAME = "fi.helsinki.cs.tmc.cli.command";

    private ProcessingEnvironment processingEnv;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, String> map = new HashMap<>();
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "Executed");

        for (Element elem : roundEnv.getElementsAnnotatedWith(Command.class)) {
            if (elem.getKind() != ElementKind.CLASS) {
                continue;
            }
            Command command = elem.getAnnotation(Command.class);
            messager.printMessage(Diagnostic.Kind.NOTE, elem.toString());
            messager.printMessage(Diagnostic.Kind.NOTE, elem.getClass().getCanonicalName());

            TypeElement classElement = (TypeElement) elem;
            map.put(command.name(), processingEnv.getElementUtils()
                    .getBinaryName(classElement).toString());
        }
        try {
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                    PACKAGE_NAME + "." + CLASS_NAME);
            messager.printMessage(Diagnostic.Kind.NOTE, "Create file");

            try (Writer writer = jfo.openWriter()) {
                BufferedWriter bwriter = new BufferedWriter(writer);
                bwriter.append("package ");
                bwriter.append(PACKAGE_NAME);
                bwriter.append(";\n\n");
                bwriter.append("import " + PACKAGE_NAME + ".CommandFactory;\n\n");
                for (Entry<String, String> entry : map.entrySet()) {
                    bwriter.append("\t\timport " + entry.getValue() + ";\n");
                }
                bwriter.append("public class " + CLASS_NAME + " {\n");
                bwriter.append("\tpublic " + CLASS_NAME + "() {\n");
                for (Entry<String, String> entry : map.entrySet()) {
                    String className = entry.getValue().split(".")[0];
                    bwriter.append("\t\tCommandFactory.createCommand(" + entry.getKey() + ", "
                            + className + ".class);\n");
                }
                bwriter.append("\t}\n");
                bwriter.append("}\n");
                bwriter.flush();
            }
        } catch (IOException ex) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Failed to create source file." + ex);
        }
        return true;
    }
}
