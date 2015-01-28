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
import org.raml.model.*;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.URIParameter;
import java.util.List;

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
        processResource("RestDocEndpoint.java", "html", "public");
        AssertJUnit.assertTrue("expected multipart info docs; got: \n" + output,
                output.contains("Note: this endpoint expects a multipart"));
    }

    @Test
    public void processControllerThatReturnsDomainObjectWithGenericParentsExpectsSuccess() {
        processResource("genericdomain/ChildController.java", "html", "public");
        AssertJUnit.assertTrue("expected firstGrandparentField and secondGrandparentField in docs; got: \n" + output,
                output.contains(">firstGrandparentField<") && output.contains(">secondGrandparentField<")
                        && output.contains(">parentField<") && output.contains(">childField<")
        );
    }

    @Test
    public void processControllerThatReturnsGenericDomainObjectExpectsSuccess() {
        processResource("genericdomain/ParentController.java", "html", "public");
        AssertJUnit.assertTrue("expected parentField in docs; got: \n" + output,
                output.contains(">parentField<"));
    }

    @Test
    public void assertQueryParams() {
        processResource("RestDocEndpoint.java", "html", "public");
        AssertJUnit.assertTrue("expected queryParam1 and queryParam2 in docs; got: \n" + output,
                               output.contains(">queryParamVal1<") && output.contains(">queryParamVal2<"));
    }

    @Test
    public void processControllerThatReturnsEnumSetExpectsSuccess() {
        processResource("EnumSetController.java", "html", "public");
        AssertJUnit.assertTrue("expected enumsets in docs; got: \n" + output,
                               output.contains(">myEnumSet<") && output.contains(">myEnum<") && output.contains(">one of [ TEST1, TEST2 ]<"));
    }

    @Test
    public void multipleBindingsForOneEndpoint() {
        processResource("RestDocEndpoint.java", "html", "public");
        AssertJUnit.assertTrue("expected multiple-bindings-a and multiple-bindings-b in docs; got: \n" + output,
                output.contains("multiple-bindings-a<") && output.contains("multiple-bindings-b<"));
    }

    // issue #29
    @Test
    public void assertNoRedundantUriParametersForResource() {
        processResource("RestDocEndpoint.java", "raml", "public");
        Raml raml = new RamlDocumentBuilder().build(output, "http://example.com");
        AssertJUnit.assertNotNull("RAML not parseable", raml);
        Resource resource = raml.getResource("/mount/api/v1/widgets/{id1}/gizmos");
        AssertJUnit.assertNotNull("Resource /mount/api/v1/widgets/{id1}/gizmos not found", resource);
        resource = resource.getResource("/{id2}");
        AssertJUnit.assertNotNull("Resource /mount/api/v1/widgets/{id1}/gizmos/{id2} not found", resource);
    }

    @Test
    public void assertUriParameterNormalization() {
        processResource("UriParameterNormalization.java", "raml", "public");
        Raml raml = new RamlDocumentBuilder().build(output, "http://example.com");
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
    public void assertAllMethods() {
        super.assertAllMethods();
        for (String format : _outputFormats) {
            processResource("AllMethods.java", format, "public");
            AssertJUnit.assertTrue(
                    "expected 'allMethodsPatch' in doc string; got: \n" + output,
                    output.contains("allMethodsPatch"));
        }
    }

    @Test
    public void testEnumsTypesQueryForRaml() {
        processResource("RestDocEndpoint.java", "raml", "public");
        Raml raml = new RamlDocumentBuilder().build(output, "http://example.com");
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
        processResource("RestDocEndpoint.java", "raml", "public");
        Raml raml = new RamlDocumentBuilder().build(output, "http://example.com");
        Resource resource = raml.getResource("/mount/api/v1/colors/{color}");
        AssertJUnit.assertNotNull("Resource /mount/api/v1/colors/{color} not found", resource);
        UriParameter up = resource.getUriParameters().get("color");
        AssertJUnit.assertNotNull("No color path param found on GET method of /mount/api/v1/colors/{color}", up);
        List<String> enums = up.getEnumeration();
        AssertJUnit.assertNotNull("Color path param on GET method of /mount/api/v1/colors/{color} not enum", enums);
        AssertJUnit.assertEquals("Color path param on GET /mount/api/v1/colors/{color} is wrong size", 3, enums.size());
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
