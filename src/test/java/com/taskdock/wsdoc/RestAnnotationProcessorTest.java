/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import freemarker.template.TemplateException;
import org.junit.Assert;
import org.junit.Test;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

public class RestAnnotationProcessorTest {

    @Test
    public void basicTest() throws Exception {
        runAnnotationProcessor();
        buildOutput();

        assertOutput();
    }

    private void buildOutput() throws ClassNotFoundException, IOException, TemplateException {
        InputStream in = new FileInputStream(Utils.SERIALIZED_RESOURCE_LOCATION);
        new RestDocAssembler().writeDocumentation(Collections.singletonList(RestDocumentation.fromStream(in)));
    }

    private void assertOutput() throws IOException {
        InputStream in;
        in = new FileInputStream(RestDocAssembler.getOutputFile());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String lines = "";
        for (String line = null; (line = reader.readLine()) != null; ) {
            lines += line + "\n";
        }

        Assert.assertTrue("expected 'JavaDoc comment' in doc string; got: \n" + lines, lines.contains("JavaDoc comment"));
    }

    private void runAnnotationProcessor() throws URISyntaxException {
        AnnotationProcessor processor = new AnnotationProcessor();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
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
