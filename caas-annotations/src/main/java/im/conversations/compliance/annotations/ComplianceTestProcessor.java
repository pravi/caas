package im.conversations.compliance.annotations;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;


@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"im.conversations.compliance.annotations.ComplianceTest"})
public class ComplianceTestProcessor extends AbstractProcessor {
    private static boolean ran = false;
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private ArrayList<TypeElement> testClasses = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ComplianceTest.class);
        for (Element e : elements) {
            if (e.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "@ComplianceTest can only be applied to a class. " +
                                e.getSimpleName() +
                                " is not a class");
                return true;
            } else {
                testClasses.add((TypeElement) e);
            }
        }
        try {
            if (!ran && !testClasses.isEmpty()) {
                writeTestsFile();
                ran = true;
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return true;
    }

    /**
     * Generates Tests class in {@link im.conversations.compliance.xmpp} package with a list of test classes, which
     * can be used to run tests
     * @throws Exception
     */
    private void writeTestsFile() throws Exception {
        JavaFileObject testsFile = filer.createSourceFile("im.conversations.compliance.xmpp.Tests");
        try (PrintWriter out = new PrintWriter(testsFile.openWriter())) {
            out.println("package im.conversations.compliance.xmpp;");
            out.println("import im.conversations.compliance.xmpp.tests.AbstractTest;");
            out.println("import java.util.Arrays;");
            out.println("import java.util.List;");
            out.println("public class Tests {");
            out.println("public static List<Class<? extends AbstractTest>> getTests() {");
            out.print("return Arrays.asList(");
            boolean first = true;
            out.println(testClasses.get(0).getQualifiedName().toString() + ".class");
            testClasses.stream().skip(1).forEach((tc -> out.println("," + tc.getQualifiedName().toString() + ".class")));
            out.println(");");
            out.println("}");
            out.println("}");
        }
    }

}