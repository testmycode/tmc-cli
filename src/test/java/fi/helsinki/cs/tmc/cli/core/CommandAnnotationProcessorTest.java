package fi.helsinki.cs.tmc.cli.core;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

public class CommandAnnotationProcessorTest {

    private CommandAnnotationProcessor processor;
    private RoundEnvironment roundEnv;
    private StringWriter stringWriter;
    private Elements mockedElementUtils;

    public CommandAnnotationProcessorTest() throws IOException {
        final JavaFileObject fileObject = mock(JavaFileObject.class);
        final Filer mockedFiler = mock(Filer.class);
        when(mockedFiler.createSourceFile(anyString())).thenReturn(fileObject);

        stringWriter = new StringWriter();
        when(fileObject.openWriter()).thenReturn(stringWriter);
        roundEnv = mock(RoundEnvironment.class);

        mockedElementUtils = mock(Elements.class);

        ProcessingEnvironment processingEnv =
                new ProcessingEnvironment() {
                    @Override
                    public Map<String, String> getOptions() {
                        return new HashMap<>();
                    }

                    @Override
                    public Messager getMessager() {
                        return null;
                    }

                    @Override
                    public Filer getFiler() {
                        return mockedFiler;
                    }

                    @Override
                    public Elements getElementUtils() {
                        return mockedElementUtils;
                    }

                    @Override
                    public Types getTypeUtils() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public SourceVersion getSourceVersion() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Locale getLocale() {
                        throw new UnsupportedOperationException();
                    }
                };
        processor = new CommandAnnotationProcessor();
        processor.init(processingEnv);
    }

    @Command(name = "commmand1", desc = "abc")
    public static class CommandAnnotationExample1 extends AbstractCommand {
        @Override
        public void getOptions(Options options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run(CliContext ctx, CommandLine args) {
            throw new UnsupportedOperationException();
        }
    }

    @Command(name = "commmand2", desc = "abc")
    public static class CommandAnnotationExample2 extends AbstractCommand {
        @Override
        public void getOptions(Options options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run(CliContext ctx, CommandLine args) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void warnsAboutInvalidElementWithCommandAnnotation() {
        final Element classElement = mock(Element.class);
        when(classElement.getKind()).thenReturn(ElementKind.FIELD);
        doReturn(new HashSet<>(Collections.singletonList(classElement)))
                .when(roundEnv)
                .getElementsAnnotatedWith(Command.class);

        Set<TypeElement> annotations = new HashSet<>();
        assertFalse(processor.process(annotations, roundEnv));
    }

    @Test
    public void worksWithoutCommands() {
        doReturn(new HashSet<>(Collections.emptyList()))
                .when(roundEnv)
                .getElementsAnnotatedWith((Class<Annotation>) any(Class.class));

        Set<TypeElement> annotations = new HashSet<>();
        assertTrue(processor.process(annotations, roundEnv));
        assertThat("" + stringWriter, not(containsString(".class")));
    }

    @Test
    public void worksWithOneCommands() {
        final Element classElement = mock(TypeElement.class);
        when(classElement.getKind()).thenReturn(ElementKind.CLASS);
        Command annotation = CommandAnnotationExample1.class.getAnnotation(Command.class);
        doReturn(annotation).when(classElement).getAnnotation(Command.class);
        doReturn(new HashSet<>(Collections.singletonList(classElement)))
                .when(roundEnv)
                .getElementsAnnotatedWith(any(Class.class));

        Name mockedName = mock(Name.class);
        when(mockedName.toString()).thenReturn("abc.TestTest");
        when(mockedElementUtils.getBinaryName((TypeElement) classElement)).thenReturn(mockedName);

        Set<TypeElement> annotations = new HashSet<>();
        assertTrue(processor.process(annotations, roundEnv));
        assertThat("" + stringWriter, containsString("TestTest.class"));
    }

    @Test
    public void generateCorrectCodeWithTwoCommands() {
        final Element classElement1 = mock(TypeElement.class);
        final Element classElement2 = mock(TypeElement.class);
        when(classElement1.getKind()).thenReturn(ElementKind.CLASS);
        when(classElement2.getKind()).thenReturn(ElementKind.CLASS);
        Command annotation = CommandAnnotationExample1.class.getAnnotation(Command.class);
        doReturn(annotation).when(classElement1).getAnnotation(Command.class);
        annotation = CommandAnnotationExample2.class.getAnnotation(Command.class);
        doReturn(annotation).when(classElement2).getAnnotation(Command.class);

        doReturn(new HashSet<>(Arrays.asList(classElement1, classElement2)))
                .when(roundEnv)
                .getElementsAnnotatedWith(any(Class.class));

        Name mockedName1 = mock(Name.class);
        when(mockedName1.toString()).thenReturn("abc.TestTest1");
        when(mockedElementUtils.getBinaryName((TypeElement) classElement1)).thenReturn(mockedName1);

        Name mockedName2 = mock(Name.class);
        when(mockedName2.toString()).thenReturn("abc.TestTest2");
        when(mockedElementUtils.getBinaryName((TypeElement) classElement2)).thenReturn(mockedName2);

        Set<TypeElement> annotations = new HashSet<>();
        assertTrue(processor.process(annotations, roundEnv));
        assertThat("" + stringWriter, containsString("(\"commmand1\", TestTest1.class)"));
        assertThat("" + stringWriter, containsString("(\"commmand2\", TestTest2.class)"));
    }
}
