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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

public class GenericDomainHierarchyAnnotationProcessorTest {
    private static String output;
    private static final TemporaryFolder tmpFolder = new TemporaryFolder();
    private static File tmpDir;

    @BeforeClass
    public static void setUp() throws IOException, URISyntaxException, ClassNotFoundException, TemplateException {
        tmpFolder.create();
        tmpDir = tmpFolder.getRoot();
//        processResource("GenericDomainController.java");
    }

    @Test
    public void processControllerThatReturnsDomainObjectWithGenericParentsExpectsSuccess() {
        processResource("ChildController.java");
        Assert.assertTrue("expected firstGrandparentField and secondGrandparentField in docs; got: \n" + output,
                output.contains(">firstGrandparentField<") && output.contains(">secondGrandparentField<")
                        && output.contains(">parentField<") && output.contains(">childField<"));
    }

    @Test
    public void processControllerThatReturnsGenericDomainObjectExpectsSuccess() {
        processResource("ParentController.java");
        Assert.assertTrue("expected parentField in docs; got: \n" + output,
                output.contains(">parentField<"));
    }


    private static void processResource(String fileName) {
        processResource(fileName, null);
    }

    private static void processResource(String fileName, Iterable<Pattern> excludes) {
        try {
            runAnnotationProcessor(tmpDir, fileName);
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

    public static void main(String[] args) throws IOException, URISyntaxException {
        File dir = new File(args[0]);
        for (int i = 1; i < args.length; i++) {
            runAnnotationProcessor(dir,
                    args[i].substring(0, args[i].lastIndexOf('/')),
                    args[i].substring(args[i].lastIndexOf('/')));
        }
    }

    private static void runAnnotationProcessor(File buildDir, final String fileName)
            throws URISyntaxException, IOException {

        runAnnotationProcessor(buildDir, "org/versly/rest/wsdoc/", fileName);
    }

    private static void runAnnotationProcessor(File buildDir, final String packagePrefix, final String fileName)
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
        Assert.assertTrue(task.call());
    }
}
