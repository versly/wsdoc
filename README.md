## Web Service Documentation Generator ##

Automatically generate up-to-date documentation for your REST API.

1. [Installation](#installation)
2. [Running](#running)
3. [Samples](#samples)
4. [Limitations](#limitations)
5. [Configuration](#configuration)
6. [Use with maven](#maven)
7. [License (ASL v2)](#license)

<a id="installation"/>
#### Installation

Currently, wsdoc is available in source format only. To install, you'll need mvn and Java 1.8 (or later) and whatnot:

    git clone git@github.com:versly/wsdoc.git
    cd wsdoc
    mvn install

Once you've done this, the wsdoc jar will be available in your local Maven repository, probably at ~/.m2/repository/org/versly/versly-wsdoc/1.1-SNAPSHOT/versly-wsdoc-1.1-SNAPSHOT.jar

Note, the Java 1.8 requirement is necessary to accommodate instructions to the annotation processor to recognize Java types introduced in 1.8.  If you intend to use wsdoc to document code that does not utilize newer types, a one-line change in AnnotationProcessor.java can be made.  Specifically, locate this line in AnnotationProcessor.java:

    @SupportedSourceVersion(SourceVersion.RELEASE_8)

and change it to something like:

    @SupportedSourceVersion(SourceVersion.RELEASE_X)

where "RELEASE_X" reflects the version of Java your build environment uses (e.g. "RELEASE_7").

<a id="running"/>
#### Running wsdoc

With version 1.1-SNAPSHOT, wsdoc now requires a Java 8 runtime at annotations processing time.  This does not impose any requirements, however, on the source version or target runtime of the processed Java code.

Often, a single REST API is implemented across a number of web archives. As a result, wsdoc is designed to run in two passes: a data-gathering pass (implemented via Java annotation processor) and an output-assembly pass (implemented as a standalone Java program):

1\. Generate the wsdoc interim data. This will create a file called org.versly.rest.wsdoc.web-service-api.ser in your build output directory. This file should be included as a resource in your web archive (at WEB-INF/classes/org.versly.rest.wsdoc.web-service-api.ser)

    javac -processor org.versly.rest.wsdoc.AnnotationProcessor *.java

2\. (Optional) Perform the rest of your WAR assembly.

3\. Generate the HTML output, given all your web archives or output from the annotation processor:

    java org.versly.rest.wsdoc.RestDocAssembler *.war *.ser

4\. Enjoy the output at web-service-api.html

Note, with release 1.1-SNAPSHOT, wsdoc requires a Java 8 runtime at annotations processing time.  This will not impose any requirements, however, on the source version or target runtime of the processed Java code.

<a id="samples"/>
#### Sample Input and Output

* Input: https://github.com/versly/wsdoc/blob/gh-pages/sample/SnowReportController.java

* Output: http://versly.github.com/wsdoc/sample/web-service-api.html

<a id="limitations"/>
#### Limitations

* wsdoc is currently limited to REST endpoints identified via the [Spring 3 web bind annotations](http://blog.springsource.com/2009/03/08/rest-in-spring-3-mvc/) (@RequestMapping and whatnot).

* wsdoc needs access to your sources to extract JavaDoc comments. If you package your DTOs in a separate compilation unit than your controllers using a build tool like mvn, the sources for those compilation units might not be available. So, wsdoc will not find the comments and will therefore not include them in the generated output. This can be resolved by providing additional source locations to apt.

* We've made a bunch of JSON-related assumptions about how you want your DTOs to be represented. None of the Jackson annotations (except @JsonIgnore) are considered, so you're pretty much left with a simple bean transformation.

* The output format is JSON-esque, but things will probably be comprehensible for non-JSON wire formats, too, assuming that our object model scanning assumptions pan out.

* Only a subset of the Spring web bind annotations are supported. This isn't by design or due to fundamental limitations; we've just only built support for the parts that we use.

<a id="configuration"/>
#### Configuration and options

* Configuring the intermediate output destination

  wsdoc runs in two distinct execution phases, and stores some data in your WARs to communicate from the first phase to the second. This data must be stored in a resource located at WEB-INF/classes/org.versly.rest.wsdoc.web-service-api.ser. Typically, this means you should run the wsdoc annotation processor with the same build output configuration (javac -d on the command line, etc.) as you use for your WAR.

* Specifying the HTML output file name

  You can specify where wsdoc should generate its output to with the --out flag when invoking org.versly.rest.wsdoc.RestDocAssembler:

        java org.versly.rest.wsdoc.RestDocAssembler --out snow-report.html *.war

* Excluding endpoint patterns

  If you have some endpoints that you'd like to exclude from the generated output, use the --exclude option to specify regular expressions to omit when invoking org.versly.rest.wsdoc.RestDocAssembler:

        java org.versly.rest.wsdoc.RestDocAssembler --out snow-report.html *.war --exclude /api/v1/admin.*

* Adding a prefix to the generated URLs

  Often, an entire WAR or a particular controller is bound to a root URL path. This information may not be available to wsdoc when processing the Java source files alone. For example, we might want to bind the snow-report endpoint in our example to /api/v1/snow-report rather than just /snow-report. To include this sort of prefix information in the generated documentation, use the [@RestApiMountPoint](https://github.com/versly/wsdoc/blob/master/org/versly/rest/wsdoc/RestApiMountPoint.java) annotation:

        @Controller
        @RestApiMountPoint("/api/v1")
        public class SnowReportController {
            ...
        }

  All endpoints inside the annotated class will be prefixed with the text provided to the annotation.  Note, this will
  be in addition to the prefixing of any class level request paths contributed by other annotations.  For example:
  
       @RestApiMountPoint("/mount/api/v1")
       @RequestMapping("/myservice")
       public class RestDocEndpoint {
           ...
       }
       
  will result in the prefixing of endpoints with "/mount/api/v1/myservice".
 
* Set Output Format

  Output can be generated in either HTML or RAML format.  The default format is HTML, however either may be specified
  using the command-line option --format.  For example:

        java org.versly.rest.wsdoc.RestDocAssembler --format raml --out snow-report.raml *.war

  or 

        java org.versly.rest.wsdoc.RestDocAssembler --format html --out snow-report.html *.war

* Controlling Publication Scope
  
  Publication scoping may be asserted using the @DocumentationScope annotation.  This annotation supports user defined
  scopes but also defines convenient constants for common scopes, such as DocumentationScope.PUBLIC and
  DocumentationScope.PRIVATE. When executing the doc assembler phase of doc generation, the --scope command line option 
  can be used to indicate a particular scope on which to filter the production of API documentation, or "all" 
  to indicate all endpoints are to be documented regardless of scope.
  
  If both an endpoint and it's containing class are explicitly annotated with a @DocumentationScope the scopes of that
  endpoint are regarded as the union of the values provided in both annotations (note an endpoint may belong to multiple
  scopes). 

     @DocumentationScope(DocumentationScope.PRIVATE)
     public static class ExperimentalController {

         @DocumentationScope(DocumentationScope.PUBLIC)
         @RequestMapping(value = "/m1", method = RequestMethod.GET)
         public void m1() {
            ...
         } 

         @RequestMapping(value = "/m2", method = RequestMethod.GET)
         public void m2() {
            ...
         } 

         @DocumentationScope("experimental")
         @RequestMapping(value = "/m3", method = RequestMethod.GET)
         public void m3() {
            ...
         }
    }
        
  In the above example, m1 will have both public and private scopes, m2 will have only private scope, and m3 will
  have private and experimental scope.  If, during the doc assembler phase, the --scope command line argument is
  provided with a value of "public", only m1 will be documented.  Likewise, if a value of "experimental" is provided
  for --scope, then only m3 will be documented.  If a value of "private" (or "all") is provided than m1, m2, and m3 will 
  all be documented.

* Generating API Level Documentation

Developers may optionally use the `@DocumentationRestApi` annotation, at the class level, to identify a class containing
rest endpoints as representing all or part of a single REST API.  If the `@DocumentationRestApi` annotation is present, 
all classes annotated with the same `id` value will be regarded by wsdoc as part of the same API and will be documented
together.  For a given `id` value, there should be one such annotated class for which the `@DocumentationRestApi`
annotation also includes a human-friendly `title` and `version`.  The javadocs of this class will also be included
in the generated documentation as the high-level overview describing the API.  

For example:

    /**
     * This is the header documentation text for RestApi2.  This API actually spans
     * multiple controller classes, RestApi2_A and RestApi2_B.  This javadoc text
     * will appear as an "overview" in the generated documentation.
     */
    @DocumentationRestApi(id = "RestApi2", title = "The RestApi2 API", version = "v1")
    @Path("/restapi2/api/v1")
    public class RestApi2_A {

        @GET
        @Path("/gadgets")
        public void getGadgets() {
        }
    }

    @DocumentationRestApi(id = "RestApi2")
    @Path("/restapi2/api/v1")
    public class RestApi2_B {

        @GET
        @Path("/whirlygigs")
        public void getWhirlygigs() {
        }
    }

The above defines a single API that spans two controller classes.  The javadocs of the `RestApi2_A` class will be used as 
the class level documentation for the API, and the resources of that API will include the union of those defined in both
`RestApi2_A` and `RestApi2_B`.  All REST endpoints defined in classes for which no `@DocumentationRestApi` annotation is 
present will be included together in a separate anonymous default API. 

If two or more APIs are present during either the annotation processing phase or the document assembly phase,
each API will result in a separate generated output document.  Endpoints that land in the anonymous default API
will be included together in a single output file with the exact name as given in the `--out` parameter of the
document assembly command.  Each other API explicitly defined by the `@DocumentationRestApi` annotation will result in 
an output file named after the `--out` parameter but with the addition of a suffix indicating the `id` of the API that 
file represents. For example, a documentation assembly phase initiated with the command:

    java org.versly.rest.wsdoc.RestDocAssembler --format raml --out snow-report.raml *.war

May result in several output files with names such as:

    snow-report.raml
    snow-report-RestApi2.raml
    snow-report-SomeOtherApi.raml

where the first contains endpoints not defined in `@DocumentationRestApi` annotated classes, and the second contains
endpoints defined in classes annotated with `@DocumentationRestApi(id = "RestApi2")`.


* Method and API Level Traits

A flexible `@DocumentationTraits` annotation can be used to tag APIs and
methods with markers that identify them as having certain characteristics.  For
example, the `@DocumentationTraits` annotation can be used to note that a given
method is deprecated, experimental, or some other developer defined tag.

    @DocumentationTraits(DocumentationTraits.EXPERIMENTAL)
    public static class RestController {

        @GET
        @Path("/method1")
        public void method1() {
        }

        @GET
        @DocumentationTraits(DocumentationTraits.DEPRECATED)
        @Path("/method2")
        public void method2() {
        }

        @GET
        @DocumentationTraits("service-scope")
        @Path("/method3")
        public void method2() {
        }
    }

In the example above, all methods inherit the `EXPERIMENTAL` trait from the
controller class. The `method2` is additionally associated with the `DEPRECATED`
trait, and `method3` is similarly tagged with a developer defined
`service-scope` which might represent the level of authorization that is
required to use that method.  During RAML documentation generation, these tags
are manifest as RAML traits in the composed RAML documentation, where they can
be subsequently augmented with text that describes the semantics of each trait.

* Method and API Level Authorization Scopes

The `@AuthorizationScope` annotation may be used to assign one or more OAuth2 authorization scopes to a given endpoint
handler or to an entire controller.  For example, a controller may be annotated to permit authorization for all contained
endpoint handlers based on one of several scopes declared at the class level, such as `two_scope_service:read` and
`two_scope_service:admin`.  It may alternatively permit authorization for particular contained endpoint handlers based on
 scopes declared at the method level, such as `two_scope_service:write`.  This might look as follows.

    @AuthorizationScope( { "two_scope_service:read", "two_scope_service:admin" } )
    public static class TwoScopeController {

        @AuthorizationScope("two_scope_service:write")
        @RequestMapping(value = "/twoscope", method = RequestMethod.POST)
        public void get() {
        }

        @RequestMapping(value = "/twoscope", method = RequestMethod.GET)
        public void post() {
        }
    }

Note that method-level declarations will override class-level declarations.

* Using templates in REST documentation

In cases where REST mount-point, description etc. are not available during the annotation processing phase, a user can annotate such values with special markers:

    @DocumentationRestApi(
            id = DocumentationRestApi.ID_TEMPLATE,
            title = DocumentationRestApi.TITLE_TEMPLATE,
            version = DocumentationRestApi.VERSION_TEMPLATE,
            mount = DocumentationRestApi.MOUNT_TEMPLATE)
    public class RestApi4 {
    }

During documentation generation, the user can pass in the actual values as follows:

    java org.versly.rest.wsdoc.RestDocAssembler \
            --template-id rest4 \
            --template-title "REST API 4" \
            --template-version v1 \
            --template-mount /restapi4/api \
            *.ser

<a id="maven"/>
#### wsdoc in a Maven build environment

    <build>
        <plugins>
            <!-- REST documentation parse -->
            <plugin>
                <groupId>org.bsc.maven</groupId>
                <artifactId>maven-processor-plugin</artifactId>
                <version>1.3.6</version>
                
                <configuration>
                    <outputDiagnostics>true</outputDiagnostics>
                    <processors>
                        <processor>org.versly.rest.wsdoc.AnnotationProcessor</processor>            
                    </processors>
                </configuration>
                        
                <executions>
                    <execution>
                        <phase>compile</phase>
                        <goals>
                            <goal>process</goal>
                        </goals>
                    </execution>
                </executions>

                <dependencies>
                    <dependency>
                        <groupId>org.versly</groupId>
                        <artifactId>versly-wsdoc</artifactId>
                        <version>1.1-SNAPSHOT</version>
                        <scope>compile</scope>
                    </dependency>
                </dependencies>
            </plugin>

            <!-- REST HTML documentation generation. Possibly in a different POM
                 than the parse step above. -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>1.2</version>

                <executions>
                    <execution>
                        <phase>prepare-package</phase>
                        <goals>
                            <goal>java</goal>
                        </goals>
                        <configuration>
                            <mainClass>org.versly.rest.wsdoc.RestDocAssembler</mainClass>
                            <arguments>
                                <argument>${project.build.directory}/classes</argument>
                            <arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

        </plugins>
    </build>

<a id="license"/>
#### License

wsdoc is licensed under the Apache Software License v2. The text of the license is available here: http://www.apache.org/licenses/LICENSE-2.0.html
