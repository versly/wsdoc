## Web Service Documentation Generator ##

Automatically generate up-to-date documentation for your REST API.

#### Running wsdoc ####

Often, a single REST API is implemented across a number of web archives. As a result, wsdoc is designed to run in two passes: a data-gathering pass (implemented via Java annotation processor) and an output-assembly pass (implemented as a standalone Java program):

1\. Generate the wsdoc interim data. This will create a file called org.versly.rest.wsdoc.web-service-api.ser in your build output directory. This file should be included as a resource in your web archive (at WEB-INF/classes/org.versly.rest.wsdoc.web-service-api.ser)

    javac -processor org.versly.rest.wsdoc.AnnotationProcessor *.java

2\. Perform the rest of your WAR assembly.

3\. Generate the HTML output, given all your web archives:

    java org.versly.rest.wsdoc.RestDocAssembler *.war

4\. Enjoy the output at web-service-api.html

#### Sample Input and Output ####

* Input: https://github.com/versly/wsdoc/blob/gh-pages/sample/SnowReportController.java

* Output: http://versly.github.com/wsdoc/sample/web-service-api.html

#### Limitations ####

* wsdoc is currently limited to REST endpoints identified via the [Spring 3 web bind annotations](http://blog.springsource.com/2009/03/08/rest-in-spring-3-mvc/) (@RequestMapping and whatnot).

* wsdoc needs access to your sources to extract JavaDoc comments. If you package your DTOs in a separate compilation unit than your controllers using a build tool like mvn, the sources for those compilation units might not be available. So, wsdoc will not find the comments and will therefore not include them in the generated output. This can be resolved by providing additional source locations to apt.

* We've made a bunch of JSON-related assumptions about how you want your DTOs to be represented. None of the Jackson annotations (except @JsonIgnore) are considered, so you're pretty much left with a simple bean transformation.

* The output format is JSON-esque, but things will probably be comprehensible for non-JSON wire formats, too, assuming that our object model scanning assumptions pan out.

* Only a subset of the Spring web bind annotations are supported. This isn't by design or due to fundamental limitations; we've just only built support for the parts that we use.

* The RestDocAssembler should run against a .ser file directly, so that you don't need to assemble a WAR prior to creating your documentation. In a build script, this isn't much of an issue, since Spring Web Services end up in a WAR anyways, but it would be more convenient for testing the generated output.

#### Configuration and options ####

* Configuring the intermediate output destination

  wsdoc runs in two distinct execution phases, and stores some data in your WARs to communicate from the first phase to the second. This data must be stored in a resource located at WEB-INF/classes/org.versly.rest.wsdoc.web-service-api.ser. Typically, this means you should run the wsdoc annotation processor with the same build output configuration (javac -d on the command line, etc.) as you use for your WAR.

* Specifying the HTML output file name

  You can specify where wsdoc should generate its output to with the --output flag when invoking org.versly.rest.wsdoc.RestDocAssembler:

        java --output snow-report.html *.war

* Adding a prefix to the generated URLs

  Often, an entire WAR or a particular controller is bound to a root URL path. This information may not be available to wsdoc when processing the Java source files alone. For example, we might want to bind the snow-report endpoint in our example to /api/v1/snow-report rather than just /snow-report. To include this sort of prefix information in the generated documentation, use the [@RestApiMountPoint](https://github.com/versly/wsdoc/blob/master/org/versly/rest/wsdoc/RestApiMountPoint.java) annotation:

        @Controller
        @RestApiMountPoint("/api/v1")
        public class SnowReportController {
            ...
        }

    All endpoints inside the annotated class will be prefixed with the text provided to the annotation.

#### wsdoc in a Maven build environment ####

    <build>
        <plugins>
            <!-- REST documentation -->
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
                        <version>1.0</version>
                        <scope>compile</scope>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>

#### License ####

wsdoc is licensed under the Apache Software License v2. The text of the license is available here: http://www.apache.org/licenses/LICENSE-2.0.html
