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

Currently, wsdoc is available in source format only. To install, you'll need mvn and java and whatnot:

    git clone git@github.com:versly/wsdoc.git
    cd wsdoc
    mvn install

Once you've done this, the wsdoc jar will be available in your local Maven repository, probably at ~/.m2/repository/org/versly/versly-wsdoc/1.0-SNAPSHOT/versly-wsdoc-1.0-SNAPSHOT.jar

<a id="running"/>
#### Running wsdoc

Often, a single REST API is implemented across a number of web archives. As a result, wsdoc is designed to run in two passes: a data-gathering pass (implemented via Java annotation processor) and an output-assembly pass (implemented as a standalone Java program):

1\. Generate the wsdoc interim data. This will create a file called org.versly.rest.wsdoc.web-service-api.ser in your build output directory. This file should be included as a resource in your web archive (at WEB-INF/classes/org.versly.rest.wsdoc.web-service-api.ser)

    javac -processor org.versly.rest.wsdoc.AnnotationProcessor *.java

2\. (Optional) Perform the rest of your WAR assembly.

3\. Generate the HTML output, given all your web archives or output from the annotation processor:

    java org.versly.rest.wsdoc.RestDocAssembler *.war *.ser

4\. Enjoy the output at web-service-api.html

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

  All endpoints inside the annotated class will be prefixed with the text provided to the annotation.

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
                        <version>1.0-SNAPSHOT</version>
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
