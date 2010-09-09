/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import freemarker.template.TemplateException;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

public class RestAnnotationProcessorTest {

    private static String output;

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnit4TestAdapter(RestAnnotationProcessorTest.class));
        return suite;
    }

    @BeforeClass
    public static void setUp() throws IOException, URISyntaxException, ClassNotFoundException, TemplateException {
        File buildDir = new File(System.getProperty("java.io.tmpdir"));
        System.setProperty("com.taskdock.wsdoc.outputFile", buildDir + "/test-wsdoc-out.html");
        runAnnotationProcessor(buildDir);
        buildOutput(buildDir);
        readOutput();
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

    private static void runAnnotationProcessor(File buildDir) throws URISyntaxException, IOException {
        AnnotationProcessor processor = new AnnotationProcessor();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(buildDir));
        JavaFileObject file = new SimpleJavaFileObject(new URI("string:///com/taskdock/wsdoc/RestDocEndpoint.java"),
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
