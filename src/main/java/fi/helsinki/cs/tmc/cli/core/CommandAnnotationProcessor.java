package fi.helsinki.cs.tmc.cli.core;

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
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"fi.helsinki.cs.tmc.cli.core.Command"})
public class CommandAnnotationProcessor extends AbstractProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandAnnotationProcessor.class);

    private static final String CLASS_NAME = "CommandList";
    private static final String PACKAGE_NAME = "fi.helsinki.cs.tmc.cli.core";
    private static final String COMMAND_PACKAGE_NAME = "fi.helsinki.cs.tmc.cli.command";
    private static final String TAB = "    ";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, String> map = new HashMap<>();

        for (Element elem : roundEnv.getElementsAnnotatedWith(Command.class)) {
            if (elem.getKind() != ElementKind.CLASS) {
                logger.warn("Element with command annotation is not class: " + elem.toString());
                return false;
            }
            Command command = elem.getAnnotation(Command.class);
            logger.info("Element with annotation: " + elem.toString());
            logger.info("Element name with annotation: " + elem.getClass().getCanonicalName());

            TypeElement classElement = (TypeElement) elem;
            map.put(
                    command.name(),
                    processingEnv.getElementUtils().getBinaryName(classElement).toString());
        }

        try {
            generateSourceFile(map);
        } catch (IOException ex) {
            logger.warn("Failed to create source file." + ex);
        }
        return true;
    }

    private void generateSourceFile(Map<String, String> map) throws IOException {
        JavaFileObject jfo =
                processingEnv.getFiler().createSourceFile(PACKAGE_NAME + "." + CLASS_NAME);

        try (Writer writer = jfo.openWriter()) {
            BufferedWriter bwriter = new BufferedWriter(writer);
            bwriter.append("package " + PACKAGE_NAME + ";\n\n");

            // import the command classes
            bwriter.append("//CHECKSTYLE:OFF\n");
            for (Entry<String, String> entry : map.entrySet()) {
                bwriter.append("import ").append(entry.getValue()).append(";\n");
            }

            bwriter.append("\npublic class " + CLASS_NAME + " {\n");
            bwriter.append(TAB + "static {\n");
            for (Entry<String, String> entry : map.entrySet()) {
                String classPath = entry.getValue();
                String[] parts = classPath.split("\\.");
                if (parts.length < 2) {
                    continue;
                }

                String className = parts[parts.length - 1];
                String packageName = "?";
                if (classPath.startsWith(COMMAND_PACKAGE_NAME)) {
                    //remove class name and dot
                    packageName = classPath.substring(
                            0,
                            classPath.length() - className.length() - 1);
                    //remove prefix
                    packageName = packageName.substring(COMMAND_PACKAGE_NAME.length());
                    //remove the first dot
                    if (packageName.startsWith(".")) {
                        packageName = packageName.substring(1);
                    }
                }
                bwriter.append(TAB + TAB + "CommandFactory.addCommand(\"")
                        .append(entry.getKey())
                        .append("\", \"")
                        .append(packageName)
                        .append("\", ")
                        .append(className)
                        .append(".class);\n");
            }
            bwriter.append(TAB + "}\n");
            bwriter.append("}\n");
            bwriter.append("//CHECKSTYLE:ON\n");
            bwriter.flush();
        }
    }
}
