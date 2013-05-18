package org.versly.rest.wsdoc;

import freemarker.template.TemplateException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.versly.rest.wsdoc.impl.RestDocumentation;
import org.versly.rest.wsdoc.impl.Utils;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

public abstract class AbstractRestAnnotationProcessorTest {
    protected static String output;
    private static File tmpDir;

    public void setUp() throws IOException, URISyntaxException, ClassNotFoundException, TemplateException {
        File tempFile = File.createTempFile("wsdoc", "tmp");
        tempFile.deleteOnExit();
        tmpDir = new File(tempFile.getParentFile(), "wsdoc-" + System.currentTimeMillis());
        tmpDir.mkdirs();
        tmpDir.deleteOnExit();
        processResource("RestDocEndpoint.java");
    }

    protected void processResource(String fileName) {
        processResource(fileName, null, true);
    }

    private void processResource(String fileName, Iterable<Pattern> excludes, boolean needsTestPackage) {
        try {
            runAnnotationProcessor(tmpDir, fileName, needsTestPackage);
            String htmlFile = tmpDir + "/" + fileName.replace(".java", ".html");
            buildOutput(tmpDir, htmlFile, excludes);
            readOutput(htmlFile);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                throw (RuntimeException) ex;
            else
                throw new RuntimeException(ex);
        }
    }

    private static void buildOutput(File buildDir, String htmlFile, Iterable<Pattern> excludes)
        throws ClassNotFoundException, IOException, TemplateException {

        InputStream in = new FileInputStream(new File(buildDir, Utils.SERIALIZED_RESOURCE_LOCATION));

        // make the parent dirs in case htmlFile is nested
        new File(htmlFile).getParentFile().mkdirs();

        new RestDocAssembler(htmlFile).writeDocumentation(
            Collections.singletonList(RestDocumentation.fromStream(in)), excludes);
    }

    private static void readOutput(String htmlFile) throws IOException {
        InputStream in = new FileInputStream(htmlFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        output = "";
        for (String line = null; (line = reader.readLine()) != null; ) {
            output += line + "\n";
        }
    }

    private void runAnnotationProcessor(File buildDir, final String fileName, boolean needsTestPackage)
            throws URISyntaxException, IOException {

        String packagePrefix = "org/versly/rest/wsdoc/" + (needsTestPackage ? getPackageToTest() + "/" : "");
        runAnnotationProcessor(buildDir, packagePrefix, fileName);
    }

    protected static void runAnnotationProcessor(File buildDir, final String packagePrefix, final String fileName)
            throws URISyntaxException, IOException {
        AnnotationProcessor processor = new AnnotationProcessor();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(buildDir));
        JavaFileObject file = new SimpleJavaFileObject(new URI("string:///" + packagePrefix + fileName),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean b) throws IOException {
                InputStream stream = getClass().getClassLoader().getResource(packagePrefix + fileName).openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String str = "";
                for (String line = null; (line = reader.readLine()) != null; str += line + "\n")
                    ;
                return str;
            }
        };
        Collection<JavaFileObject> files = Collections.singleton(file);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, files);
        task.setProcessors(Collections.singleton(processor));
        AssertJUnit.assertTrue(task.call());
    }

    @Test
    public void assertJavaDocComments() {
        processResource("RestDocEndpoint.java");
        AssertJUnit.assertTrue(
                "expected 'JavaDoc comment' in doc string; got: \n" + output,
                output.contains("JavaDoc comment"));
    }

    @Test
    public void assertReturnValueComments() {
        processResource("RestDocEndpoint.java");
        AssertJUnit.assertTrue("expected \"exciting return value's date\" in doc string; got: \n" + output,
            output.contains("exciting return value's date"));
    }

    @Test
    public void assertPathVariableWithOverriddenName() {
        processResource("RestDocEndpoint.java");
        AssertJUnit.assertTrue("expected \"dateParam\" in doc string; got: \n" + output,
            output.contains("dateParam"));
    }

    @Test
    public void assertParams() {
        processResource("RestDocEndpoint.java");
        AssertJUnit.assertTrue("expected param0 and param1 in docs; got: \n" + output,
            output.contains(">param0<") && output.contains(">param1<"));
    }

    @Test
    public void assertOverriddenPaths() {
        processResource("RestDocEndpoint.java");
        AssertJUnit.assertTrue("expected multiple voidreturn sections; got: \n" + output,
            output.indexOf("<a id=\"/mount/voidreturn") != output.lastIndexOf("<a id=\"/mount/voidreturn"));
    }

    @Test
    public void assertUuidIsNotTraversedInto() {
        processResource("RestDocEndpoint.java");
        AssertJUnit.assertFalse(
                "leastSignificantBits field (member field of UUID class) should not be in output",
                output.contains("leastSignificantBits"));
        AssertJUnit.assertTrue("expected uuid type somewhere in doc",
            output.contains("json-primitive-type\">uuid<"));
    }

    @Test
    public void generateExample() {
        processResource("SnowReportController.java");
    }

    @Test
    public void nonRecursiveTypeWithMultipleUsesDoesNotHaveRecursionCircles() {
        processResource("NonRecursiveMultiUse.java");
        AssertJUnit.assertFalse("should not contain the recursion symbol",
                output.contains("&#x21ba;"));
    }

    @Test
    public void excludePatterns() {
        processResource("SnowReportController.java",
                Arrays.asList(Pattern.compile("foo"), Pattern.compile(".*snow-report.*")), true);
        AssertJUnit.assertFalse("should not contain the snow-report endpoint",
            output.contains("snow-report"));
    }

    @Test
    public void genericTypeResolution() throws IOException, URISyntaxException {
        processResource("RestDocEndpoint.java");
    }

    protected abstract String getPackageToTest();
}
