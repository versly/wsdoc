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

import freemarker.template.TemplateException;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

public class RestAnnotationProcessorTest {
    private static String output;
    private static final TemporaryFolder tmpFolder = new TemporaryFolder();

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnit4TestAdapter(RestAnnotationProcessorTest.class));
        return suite;
    }

    @BeforeClass
    public static void setUp() throws IOException, URISyntaxException, ClassNotFoundException, TemplateException {
        tmpFolder.create();
        File tmpDir = tmpFolder.getRoot();
        System.setProperty(RestDocAssembler.OUTPUT_FILE_PROPERTY, tmpDir + "/test-wsdoc-out.html");
        runAnnotationProcessor(tmpDir);
        buildOutput(tmpDir);
        readOutput();
    }

    @AfterClass
    public static void tearDown() {
        tmpFolder.delete();
    }

    private static void readOutput() throws IOException {
        InputStream in;
        in = new FileInputStream(RestDocAssembler.getOutputFile());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        output = "";
        for (String line = null; (line = reader.readLine()) != null; ) {
            output += line + "\n";
        }
    }

    private static void buildOutput(File buildDir) throws ClassNotFoundException, IOException, TemplateException {
        InputStream in = new FileInputStream(new File(buildDir, Utils.SERIALIZED_RESOURCE_LOCATION));
        new RestDocAssembler().writeDocumentation(Collections.singletonList(RestDocumentation.fromStream(in)));
    }

    @Test
    public void assertJavaDocComments() {
        Assert.assertTrue("expected 'JavaDoc comment' in doc string; got: \n" + output,
            output.contains("JavaDoc comment"));
    }

    @Test
    public void assertReturnValueComments() {
        Assert.assertTrue("expected \"exciting return value's date\" in doc string; got: \n" + output,
            output.contains("exciting return value's date"));
    }

    @Test
    public void assertPathVariableWithOverriddenName() {
        Assert.assertTrue("expected \"dateParam\" in doc string; got: \n" + output,
            output.contains("dateParam"));
    }

    @Test
    public void assertParams() {
        Assert.assertTrue("expected param0 and param1 in docs; got: \n" + output,
            output.contains(">param0<") && output.contains(">param1<"));
    }

    @Test
    public void assertMultipart() {
        Assert.assertTrue("expected multipart info docs; got: \n" + output,
            output.contains("Note: this endpoint expects a multipart"));
    }

    @Test
    public void assertOverriddenPaths() {
        Assert.assertTrue("expected multiple voidreturn sections; got: \n" + output,
            output.indexOf("<a id=\"/mount/voidreturn") != output.lastIndexOf("<a id=\"/mount/voidreturn")); 
    }

    @Test
    public void assertUuidIsNotTraversedInto() {
        Assert.assertFalse("leastSignificantBits field (member field of UUID class) should not be in output",
            output.contains("leastSignificantBits"));
        Assert.assertTrue("expected uuid type somewhere in doc",
            output.contains("json-primitive-type\">uuid<"));
    }

    private static void runAnnotationProcessor(File buildDir) throws URISyntaxException, IOException {
        AnnotationProcessor processor = new AnnotationProcessor();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(buildDir));
        JavaFileObject file = new SimpleJavaFileObject(new URI("string:///com/versly/wsdoc/RestDocEndpoint.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean b) throws IOException {
                InputStream stream = getClass().getResource("RestDocEndpoint.java").openStream();
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
