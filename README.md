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

* Output: https://github.com/versly/wsdoc/blob/gh-pages/sample/web-service-api.html

#### Limitations ####

* wsdoc is currently limited to REST endpoints identified via the Spring 3 Web Services annotations (@RequestMapping and whatnot).

* Only a subset of the Spring Web Services annotations are supported. This isn't by design or due to fundamental limitations; we've just only built support for the parts that we use.

* The -Dorg.versly.rest.wsdoc.outputFile syntax for specifying the HTML output file is the result of a legacy of communicating data to an AnnotationProcessor, and is super lame. It should be deprecated in favor of a proper command-line argument.

* The RestDocAssembler should run against a .ser file directly, so that you don't need to assemble a WAR prior to creating your documentation. In a build script, this isn't much of an issue, since Spring Web Services end up in a WAR anyways, but it would be more convenient for testing the generated output.

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
                        <artifactId>rest.wsdoc</artifactId>
                        <version>1.0</version>
                        <scope>compile</scope>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>

#### License ####

wsdoc is licensed under the Apache Software License v2. The text of the license is available here: http://www.apache.org/licenses/LICENSE-2.0.html
