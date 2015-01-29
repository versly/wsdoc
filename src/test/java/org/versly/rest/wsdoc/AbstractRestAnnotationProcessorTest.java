package org.versly.rest.wsdoc;

import freemarker.template.TemplateException;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.versly.rest.wsdoc.impl.RestDocumentation;
import org.versly.rest.wsdoc.impl.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public abstract class AbstractRestAnnotationProcessorTest {
    protected static Map<String,String> output;
    protected static String defaultApiOutput;
    private static File tmpDir;
    protected static final String[] _outputFormats = { "html", "raml" };

    public void setUp() throws IOException, URISyntaxException, ClassNotFoundException, TemplateException {
        File tempFile = File.createTempFile("wsdoc", "tmp");
        tempFile.deleteOnExit();
        tmpDir = new File(tempFile.getParentFile(), "wsdoc-" + System.currentTimeMillis());
        tmpDir.mkdirs();
        tmpDir.deleteOnExit();
    }

    protected void processResource(String fileName, String outputFormat, String scope) {
        processResource(fileName, outputFormat, null, scope, true);
    }

    private void processResource(
            String fileName, String outputFormat, Iterable<Pattern> excludes, 
            String scope, boolean needsTestPackage) {
        try {
            runAnnotationProcessor(tmpDir, fileName, needsTestPackage);
            String outputFile = tmpDir + "/" + fileName.replace(".java", "." + outputFormat);
            List<String> filesWritten = buildOutput(tmpDir, outputFile, outputFormat, excludes, scope);
            readOutput(outputFile, filesWritten);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                throw (RuntimeException) ex;
            else
                throw new RuntimeException(ex);
        }
    }

    private static List<String> buildOutput(
            File buildDir, String outputFile, String outputFormat, Iterable<Pattern> excludes, String scope)
        throws ClassNotFoundException, IOException, TemplateException {

        final InputStream in = new FileInputStream(new File(buildDir, Utils.SERIALIZED_RESOURCE_LOCATION));

        // make the parent dirs in case htmlFile is nested
        new File(outputFile).getParentFile().mkdirs();

        return new RestDocAssembler(outputFile, outputFormat).writeDocumentation(
                new LinkedList<RestDocumentation>() {{ add(RestDocumentation.fromStream(in)); }}, excludes, scope);
    }

    private static void readOutput(String outputFile, List<String> filesWritten) throws IOException {
        output = new LinkedHashMap<String, String>();
        for (String fileWritten : filesWritten) {
            InputStream in = new FileInputStream(fileWritten);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String fileContent = "";
            for (String line = null; (line = reader.readLine()) != null; ) {
                fileContent += line + "\n";
            }
            output.put(fileWritten, fileContent);
            if (fileWritten.equals(outputFile)) {
                defaultApiOutput = fileContent;
            }
        }
    }

    private void runAnnotationProcessor(File buildDir, final String fileName, boolean needsTestPackage)
            throws URISyntaxException, IOException {

        String packagePrefix = "org/versly/rest/wsdoc/" + (needsTestPackage ? getPackageToTest() + "/" : "");
        runAnnotationProcessor(buildDir, packagePrefix, fileName);
    }

    protected static void runAnnotationProcessor(
            File buildDir, final String packagePrefix, final String fileName)
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
        for (String format: _outputFormats) {
            processResource("RestDocEndpoint.java", format, "all");
            AssertJUnit.assertTrue(
                    "expected 'JavaDoc comment' in doc string; got: \n" + defaultApiOutput,
                    defaultApiOutput.contains("JavaDoc comment"));
        }
    }

    @Test
    public void assertReturnValueComments() {
        processResource("RestDocEndpoint.java", "html", "all");
        AssertJUnit.assertTrue("expected \"exciting return value's date\" in doc string; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("exciting return value's date"));
    }

    @Test
    public void assertPathVariableWithOverriddenName() {
        for (String format: _outputFormats) {
            processResource("RestDocEndpoint.java", format, "all");
            AssertJUnit.assertTrue("expected \"dateParam\" in doc string; got: \n" + defaultApiOutput,
                    defaultApiOutput.contains("dateParam"));
        }
    }

    @Test
    public void assertParams() {
        processResource("RestDocEndpoint.java", "html", "all");
        AssertJUnit.assertTrue("expected param0 and param1 in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">param0<") && defaultApiOutput.contains(">param1<"));
    }

    @Test
    public void assertOverriddenPaths() {
        processResource("RestDocEndpoint.java", "html", "all");
        AssertJUnit.assertTrue("expected multiple voidreturn sections; got: \n" + defaultApiOutput,
                defaultApiOutput.indexOf("<a id=\"/mount/api/v1/voidreturn") != defaultApiOutput.lastIndexOf("<a id=\"/mount/api/v1/voidreturn"));
    }

    @Test
    public void assertUuidIsNotTraversedInto() {
        processResource("RestDocEndpoint.java", "html", "all");
        AssertJUnit.assertFalse(
                "leastSignificantBits field (member field of UUID class) should not be in results",
                defaultApiOutput.contains("leastSignificantBits"));
        AssertJUnit.assertTrue("expected uuid type somewhere in doc",
                defaultApiOutput.contains("json-primitive-type\">uuid<"));
    }

    @Test
    public void generateExample() {
        for (String format: _outputFormats) {
            processResource("SnowReportController.java", format, "all");
        }
    }

    @Test
    public void nonRecursiveTypeWithMultipleUsesDoesNotHaveRecursionCircles() {
        for (String format: _outputFormats) {
            processResource("NonRecursiveMultiUse.java", format, "all");
            AssertJUnit.assertFalse("should not contain the recursion symbol",
                    defaultApiOutput.contains("&#x21ba;"));
        }
    }

    @Test
    public void assertAllMethods() {
        for (String format : _outputFormats) {
            processResource("AllMethods.java", format, "all");
            AssertJUnit.assertTrue(
                    "expected 'allMethodsGet' in doc string; got: \n" + defaultApiOutput,
                    defaultApiOutput.contains("allMethodsGet"));
            AssertJUnit.assertTrue(
                    "expected 'allMethodsPost' in doc string; got: \n" + defaultApiOutput,
                    defaultApiOutput.contains("allMethodsPost"));
            AssertJUnit.assertTrue(
                    "expected 'allMethodsPut' in doc string; got: \n" + defaultApiOutput,
                    defaultApiOutput.contains("allMethodsPut"));
            AssertJUnit.assertTrue(
                    "expected 'allMethodsDelete' in doc string; got: \n" + defaultApiOutput,
                    defaultApiOutput.contains("allMethodsDelete"));
        }
    }

    @Test
    public void excludePatterns() {
        for (String format: _outputFormats) {
            processResource("SnowReportController.java", format,
                    Arrays.asList(Pattern.compile("foo"), Pattern.compile(".*snow-report.*")), "all", true);
            AssertJUnit.assertFalse("should not contain the snow-report endpoint",
                    defaultApiOutput.contains("snow-report"));
        }
    }

    @Test
    public void genericTypeResolution() throws IOException, URISyntaxException {
        for (String format: _outputFormats) {
            processResource("RestDocEndpoint.java", format, "all");
        }
    }

    // issue #29
    @Test
    public void assertNoRedundantUriParametersForResource() {
        processResource("RestDocEndpoint.java", "raml", "all");
        Raml raml = new RamlDocumentBuilder().build(defaultApiOutput, "http://example.com");
        AssertJUnit.assertNotNull("RAML not parseable", raml);
        Resource resource = raml.getResource("/mount/api/v1/widgets/{id1}/gizmos");
        AssertJUnit.assertNotNull("Resource /mount/api/v1/widgets/{id1}/gizmos not found", resource);
        resource = resource.getResource("/{id2}");
        AssertJUnit.assertNotNull("Resource /mount/api/v1/widgets/{id1}/gizmos/{id2} not found", resource);
    }

    @Test
    public void assertUriParameterNormalization() {
        processResource("UriParameterNormalization.java", "raml", "all");
        Raml raml = new RamlDocumentBuilder().build(defaultApiOutput, "http://example.com");
        AssertJUnit.assertNotNull("RAML not parseable", raml);
        Resource resource = raml.getResource("/widgets/{id}");
        AssertJUnit.assertNotNull("Resource /widgets/{id} not found", resource);
        UriParameter id = resource.getUriParameters().get("id");
        AssertJUnit.assertNotNull("Resource /widgets/{id} has no id URI parameter", id);
        AssertJUnit.assertEquals("Resource /widgets/{id} id URI parameter description is wrong",
                "The widget identifier.", id.getDescription().trim());
        resource = resource.getResource("/gadgets");
        AssertJUnit.assertNotNull("Resource /widgets/{id}/gadgets not found", resource);
        id = resource.getUriParameters().get("id");
        AssertJUnit.assertNull("Resource /widgets/{id}/gadgets has it's own id URI parameter when it should not", id);
    }

    @Test
    public void testEnumsTypesQueryForRaml() {
        processResource("RestDocEndpoint.java", "raml", "all");
        Raml raml = new RamlDocumentBuilder().build(defaultApiOutput, "http://example.com");
        Resource resource = raml.getResource("/mount/api/v1/whirlygigs");
        AssertJUnit.assertNotNull("Resource /mount/api/v1/whirlygigs not found", resource);
        Action action = resource.getAction(ActionType.GET);
        AssertJUnit.assertNotNull("Method GET not found on /mount/api/v1/whirlygigs", action);
        QueryParameter qp = action.getQueryParameters().get("color");
        AssertJUnit.assertNotNull("No color query param found on GET method of /mount/api/v1/whirlygigs", qp);
        List<String> enums = qp.getEnumeration();
        AssertJUnit.assertNotNull("Color query param on GET method of /mount/api/v1/whirlygigs not enum", enums);
        AssertJUnit.assertEquals("Color query param on GET /mount/api/v1/whirlygigs is wrong size", 3, enums.size());
    }

    @Test
    public void testEnumsTypesInPathForRaml() {
        processResource("RestDocEndpoint.java", "raml", "all");
        Raml raml = new RamlDocumentBuilder().build(defaultApiOutput, "http://example.com");
        Resource resource = raml.getResource("/mount/api/v1/colors/{color}");
        AssertJUnit.assertNotNull("Resource /mount/api/v1/colors/{color} not found", resource);
        UriParameter up = resource.getUriParameters().get("color");
        AssertJUnit.assertNotNull("No color path param found on GET method of /mount/api/v1/colors/{color}", up);
        List<String> enums = up.getEnumeration();
        AssertJUnit.assertNotNull("Color path param on GET method of /mount/api/v1/colors/{color} not enum", enums);
        AssertJUnit.assertEquals("Color path param on GET /mount/api/v1/colors/{color} is wrong size", 3, enums.size());
    }
    
    @Test
    public void testPublicationScopes() {
        for (String format : _outputFormats) {
            processResource("PublicationScopes.java", format, "all");
            AssertJUnit.assertTrue(defaultApiOutput.contains("/public1"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/public2"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/private2"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/private3"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/private4"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/pubpriv4"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/public5/foo"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/pubpriv5/bar"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/newshakystuff/foo"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/newshakystuff/bar"));

            processResource("PublicationScopes.java", format, "public");
            AssertJUnit.assertTrue(defaultApiOutput.contains("/public2"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/private2"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/private3"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/private4"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/pubpriv4"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/public5/foo"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/pubpriv5/bar"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/newshakystuff/foo"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/newshakystuff/bar"));

            processResource("PublicationScopes.java", format, "private");
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/public1"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/public2"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/private2"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/private3"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/private4"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/pubpriv4"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/public5/foo"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/pubpriv5/bar"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/newshakystuff/foo"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/newshakystuff/bar"));

            processResource("PublicationScopes.java", format, "experimental");
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/public1"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/public2"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/private2"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/private3"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/private4"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/pubpriv4"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/public5/foo"));
            AssertJUnit.assertTrue(!defaultApiOutput.contains("/pubpriv5/bar"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/newshakystuff/foo"));
            AssertJUnit.assertTrue(defaultApiOutput.contains("/newshakystuff/bar"));
        }
    }

    protected abstract String getPackageToTest();
}
