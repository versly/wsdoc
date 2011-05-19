/*
 * Copyright 2011 TaskDock, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.versly.rest.wsdoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import freemarker.template.TemplateException;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RestAnnotationProcessorTest {
    private static String output;
    private static final TemporaryFolder tmpFolder = new TemporaryFolder();
    private static File tmpDir;

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnit4TestAdapter(RestAnnotationProcessorTest.class));
        return suite;
    }

    @BeforeClass
    public static void setUp() throws IOException, URISyntaxException, ClassNotFoundException, TemplateException {
        tmpFolder.create();
        tmpDir = tmpFolder.getRoot();
        processResource("RestDocEndpoint.java");
    }

    @AfterClass
    public static void tearDown() {
        tmpFolder.delete();
    }

    @Test
    public void assertJavaDocComments() {
        processResource("RestDocEndpoint.java");
        Assert.assertTrue(
            "expected 'JavaDoc comment' in doc string; got: \n" + output,
            output.contains("JavaDoc comment"));
    }

    @Test
    public void assertReturnValueComments() {
        processResource("RestDocEndpoint.java");
        Assert.assertTrue("expected \"exciting return value's date\" in doc string; got: \n" + output,
            output.contains("exciting return value's date"));
    }

    @Test
    public void assertPathVariableWithOverriddenName() {
        processResource("RestDocEndpoint.java");
        Assert.assertTrue("expected \"dateParam\" in doc string; got: \n" + output,
            output.contains("dateParam"));
    }

    @Test
    public void assertParams() {
        processResource("RestDocEndpoint.java");
        Assert.assertTrue("expected param0 and param1 in docs; got: \n" + output,
            output.contains(">param0<") && output.contains(">param1<"));
    }

    @Test
    public void assertMultipart() {
        processResource("RestDocEndpoint.java");
        Assert.assertTrue("expected multipart info docs; got: \n" + output,
            output.contains("Note: this endpoint expects a multipart"));
    }

    @Test
    public void assertOverriddenPaths() {
        processResource("RestDocEndpoint.java");
        Assert.assertTrue("expected multiple voidreturn sections; got: \n" + output,
            output.indexOf("<a id=\"/mount/voidreturn") != output.lastIndexOf("<a id=\"/mount/voidreturn")); 
    }

    @Test
    public void assertUuidIsNotTraversedInto() {
        processResource("RestDocEndpoint.java");
        Assert.assertFalse(
            "leastSignificantBits field (member field of UUID class) should not be in output",
            output.contains("leastSignificantBits"));
        Assert.assertTrue("expected uuid type somewhere in doc",
            output.contains("json-primitive-type\">uuid<"));
    }

    @Test
    public void generateExample() {
        processResource("SnowReportController.java");
    }

    @Test
    public void nonRecursiveTypeWithMultipleUsesDoesNotHaveRecursionCircles() {
        processResource("NonRecursiveMultiUse.java");
        Assert.assertFalse("should not contain the recursion symbol",
            output.contains("&#x21ba;"));
    }

    private static void processResource(String fileName) {
        try {
            runAnnotationProcessor(tmpDir, fileName);
            String htmlFile = tmpDir + "/" + fileName.replace(".java", ".html");
            buildOutput(tmpDir, htmlFile);
            readOutput(htmlFile);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                throw (RuntimeException) ex;
            else
                throw new RuntimeException(ex);
        }
    }

    private static void buildOutput(File buildDir, String htmlFile)
        throws ClassNotFoundException, IOException, TemplateException {

        InputStream in = new FileInputStream(new File(buildDir, Utils.SERIALIZED_RESOURCE_LOCATION));
        new RestDocAssembler(htmlFile).writeDocumentation(
            Collections.singletonList(RestDocumentation.fromStream(in)));
    }

    private static void readOutput(String htmlFile) throws IOException {
        InputStream in = new FileInputStream(htmlFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        output = "";
        for (String line = null; (line = reader.readLine()) != null; ) {
            output += line + "\n";
        }
    }

    private static void runAnnotationProcessor(File buildDir, final String fileName)
            throws URISyntaxException, IOException {
        AnnotationProcessor processor = new AnnotationProcessor();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(buildDir));
        JavaFileObject file = new SimpleJavaFileObject(new URI("string:///org/versly/rest/wsdoc/" + fileName),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean b) throws IOException {
                InputStream stream = getClass().getResource(fileName).openStream();
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
        Assert.assertTrue(task.call());
    }
}
