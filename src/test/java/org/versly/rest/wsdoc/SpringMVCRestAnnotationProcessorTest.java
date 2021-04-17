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
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class SpringMVCRestAnnotationProcessorTest extends AbstractRestAnnotationProcessorTest {

    @BeforeClass
    public void setUp() throws IOException, ClassNotFoundException, URISyntaxException, TemplateException {
        super.setUp();
    }

    @Override
    protected String getPackageToTest() {
        return "springmvc";
    }

    @Test
    public void assertMultipart() {
        processResource("RestDocEndpoint.java", "html", "all");
        AssertJUnit.assertTrue("expected multipart info docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("Note: this endpoint expects a multipart"));
    }

    @Test
    public void processControllerThatReturnsDomainObjectWithGenericParentsExpectsSuccess() {
        processResource("genericdomain/ChildController.java", "html", "all");
        AssertJUnit.assertTrue("expected firstGrandparentField and secondGrandparentField in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">firstGrandparentField<") && defaultApiOutput.contains(">secondGrandparentField<")
                        && defaultApiOutput.contains(">parentField<") && defaultApiOutput.contains(">childField<")
        );
    }

    @Test
    public void processControllerThatReturnsGenericDomainObjectExpectsSuccess() {
        processResource("genericdomain/ParentController.java", "html", "all");
        AssertJUnit.assertTrue("expected parentField in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">parentField<"));
    }

    @Test
    public void assertQueryParams() {
        processResource("RestDocEndpoint.java", "html", "all");
        AssertJUnit.assertTrue("expected queryParam1 and queryParam2 in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">queryParamVal1<") && defaultApiOutput.contains(">queryParamVal2<"));
    }

    @Test
    public void processControllerThatReturnsEnumSetExpectsSuccess() {
        processResource("EnumSetController.java", "html", "all");
        AssertJUnit.assertTrue("expected enumsets in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">myEnumSet<") && defaultApiOutput.contains(">myEnum<") && defaultApiOutput.contains(">one of [ TEST1, TEST2 ]<"));
    }

    @Test
    public void multipleBindingsForOneEndpoint() {
        processResource("RestDocEndpoint.java", "html", "all");
        AssertJUnit.assertTrue("expected multiple-bindings-a and multiple-bindings-b in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("multiple-bindings-a<") && defaultApiOutput.contains("multiple-bindings-b<"));
    }

    @Test
    public void assertOptionalIsNotTraversedInto() {
        processResource("RestDocEndpoint.java", "raml", "all");
        AssertJUnit.assertFalse(
                "'present' field (member field of Optional class) should not be in results",
                defaultApiOutput.contains("present"));
        AssertJUnit.assertFalse(
                "'Optional<' should not be in results",
                defaultApiOutput.contains("Optional<"));
        AssertJUnit.assertTrue(defaultApiOutput,
                defaultApiOutput.contains("optionalfield"));
    }

    @Test
    public void assertAllMethods() {
        super.assertAllMethods();
        for (String format : _outputFormats) {
            processResource("AllMethods.java", format, "all");
            AssertJUnit.assertTrue(
                    "expected 'allMethodsPatch' in doc string; got: \n" + defaultApiOutput,
                    defaultApiOutput.contains("allMethodsPatch"));
        }
    }

    @Test
    public void assertWildcardTypes() {
        processResource("genericdomain/WildcardController.java", "html", "all");
        AssertJUnit.assertTrue("expected 'wildcardType' in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">wildcardType<"));
    }

    @Test
    public void assertAsync() {
        processResource("genericdomain/AsyncController.java", "html", "all");
        AssertJUnit.assertFalse("expected no AsyncWebTask fields in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">callable<"));
        AssertJUnit.assertTrue("expected 'childField' in docs; got: \n" + defaultApiOutput,
                defaultApiOutput.contains(">childField<"));
    }

    @Test
    public void assertSpring43ComposedAnnotationController() {

        super.assertAllMethods();

        for (String format : _outputFormats) {

            processResource("Spring43ComposedAnnotationController.java", format, "all");

            AssertJUnit.assertTrue(
                "expected 'theGetMethod' in doc string; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("theGetMethod"));
            AssertJUnit.assertTrue(
                "expected 'thePostMethod' in doc string; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("thePostMethod"));
            AssertJUnit.assertTrue(
                "expected 'thePutMethod' in doc string; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("thePutMethod"));
            AssertJUnit.assertTrue(
                "expected 'theDeleteMethod' in doc string; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("theDeleteMethod"));
            AssertJUnit.assertTrue(
                "expected 'thePatchMethod' in doc string; got: \n" + defaultApiOutput,
                defaultApiOutput.contains("thePatchMethod"));
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
}
