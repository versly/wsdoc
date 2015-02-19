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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.versly.rest.wsdoc.impl.RestDocumentation;
import org.versly.rest.wsdoc.impl.Utils;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class RestDocAssembler {
    private final String _outputFileName;
    private final String _outputTemplate;

    public static void main(String... args)
        throws IOException, ClassNotFoundException, TemplateException {
        Arguments arguments = new Arguments();
        new JCommander(arguments, args);

        List<RestDocumentation> docs = new LinkedList<RestDocumentation>();
        for (String input : arguments.inputs) {
            File inputFile = new File(input);
            if (inputFile.isDirectory()) {
                System.err.println("adding web service docs from classes directory " + input);
                File resourceFile = new File(inputFile, Utils.SERIALIZED_RESOURCE_LOCATION);
                docs.add(RestDocumentation.fromStream(new FileInputStream(resourceFile))); // TODO resource management
            } else if (input.toLowerCase().endsWith(".war")) {
                System.err.println("adding web service docs from WAR " + input);
                JarFile jar = new JarFile(input);
                ZipEntry e = jar.getEntry("WEB-INF/classes/" + Utils.SERIALIZED_RESOURCE_LOCATION);
                docs.add(RestDocumentation.fromStream(jar.getInputStream(e)));
                jar.close();
            } else {
                System.err.println("adding web service docs from serialized input " + input);
                docs.add(RestDocumentation.fromStream(new FileInputStream(inputFile))); // TODO resource management
            }
        }

        if (docs.size() > 0) {
            List<Pattern> excludePatterns = new ArrayList<Pattern>();
            for (String pattern : arguments.excludes)
                excludePatterns.add(Pattern.compile(pattern));
            new RestDocAssembler(arguments.outputFileName, arguments.outputFormat)
                    .writeDocumentation(docs, excludePatterns, arguments.scope);
        }
    }

    public RestDocAssembler(String outputFileName, String outputFormat) {

        _outputFileName = outputFileName;
        if (outputFormat.equalsIgnoreCase("raml"))
        {
            _outputTemplate = "RamlDocumentation.ftl";
        }
        else
        {
            _outputTemplate = "RestDocumentation.ftl";
        }
    }

    public RestDocAssembler(String outputFileName) {
        this(outputFileName, "html");
    }

    List<String> writeDocumentation(List<RestDocumentation> docs, Iterable<Pattern> excludePatterns, String scope)
        throws IOException, ClassNotFoundException, TemplateException {
        List<String> filesWritten = new ArrayList<String>();
        
        // combine APIs from the REST docs into one map, merging those with matching identifiers
        Map<String,RestDocumentation.RestApi> aggregatedApis = new LinkedHashMap<String,RestDocumentation.RestApi>();
        for (RestDocumentation doc : docs) {
            for (RestDocumentation.RestApi api : doc.getApis()) {
                if (!aggregatedApis.containsKey(api.getIdentifier())) {
                    aggregatedApis.put(api.getIdentifier(), api);
                }
                else {
                    aggregatedApis.get(api.getIdentifier()).merge(api);
                }
            }
        }

        // filter doc objects by client provided exclude patterns
        Collection<RestDocumentation.RestApi> filteredApis = null;
        if (excludePatterns != null) {
            filteredApis = new LinkedList<RestDocumentation.RestApi>();
            for (RestDocumentation.RestApi api : aggregatedApis.values())
                filteredApis.add(api.filter(excludePatterns));
        } else {
            filteredApis = aggregatedApis.values();
        }
        
        // derive the common base URI for all resources of each API and make that the API mount
        for (RestDocumentation.RestApi api : aggregatedApis.values()) {
            String basePath = null;
            for (RestDocumentation.RestApi.Resource resource : api.getResources()) {
                String resourcePath = resource.getPath();
                if (null != resourcePath) {
                    if (null == basePath) {
                        basePath = resourcePath;
                    }
                    else {
                        String[] baseParts = basePath.split("/");
                        String[] resourceParts = resourcePath.split("/");
                        basePath = "";
                        int smallerLength = Math.min(baseParts.length, resourceParts.length);
                        for (int i = 0; i < smallerLength && baseParts[i].equals(resourceParts[i]); ++i) {
                            if (baseParts[i].length() > 0) {
                                basePath += "/" + baseParts[i];
                            }
                        }
                    }
                }
            }
            api.setMount(basePath);
        }

        // use command-line --scope value to set the scope on which to filter the generated documentation
        if (!scope.equals("all")) {
            HashSet<String> requestedScopes = new HashSet<String>(Arrays.asList(new String[]{scope}));

            // ugly old-style iterating because we need to be able to remove elements as we go
            Iterator<RestDocumentation.RestApi> apiIter = filteredApis.iterator();
            while (apiIter.hasNext()) {
                RestDocumentation.RestApi api = apiIter.next();
                Iterator<RestDocumentation.RestApi.Resource> resIter = api.getResources().iterator();
                while (resIter.hasNext()) {
                    RestDocumentation.RestApi.Resource resource = resIter.next();
                    Iterator<RestDocumentation.RestApi.Resource.Method> methIter = resource.getRequestMethodDocs().iterator();
                    while (methIter.hasNext()) {
                        HashSet<String> scopes = methIter.next().getScopes();
                        scopes.retainAll(requestedScopes);
                        if (scopes.isEmpty()) {
                            methIter.remove();
                        }
                    }
                    if (resource.getRequestMethodDocs().isEmpty()) {
                        resIter.remove();
                    }
                }
                if (api.getResources().isEmpty()) {
                    apiIter.remove();
                }
            }
        }
        
        Configuration conf = new Configuration();
        conf.setClassForTemplateLoading(RestDocAssembler.class, "");
        conf.setObjectWrapper(new DefaultObjectWrapper());
        Writer out = null;
        try {
            for (RestDocumentation.RestApi api : filteredApis) {
                Template template = conf.getTemplate(_outputTemplate);
                Map<String, RestDocumentation.RestApi> root = new HashMap<String, RestDocumentation.RestApi>();
                root.put("api", api);
                String fileName = getOutputFileName(api);
                filesWritten.add(fileName);
                File file = new File(fileName);
                out = new FileWriter(file);
                template.process(root, out);
                out.flush();
                System.err.printf("Wrote REST docs to %s\n", file.getAbsolutePath());
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
        return filesWritten;
    }

    String getOutputFileName(RestDocumentation.RestApi api) {
        String outputFileName = _outputFileName;
        if (!api.getIdentifier().equals(RestDocumentation.RestApi.DEFAULT_IDENTIFIER)) {
            StringBuilder constructedName = new StringBuilder(_outputFileName);
            int identifierIndex = constructedName.lastIndexOf(".");
            if (identifierIndex < 0) {
                identifierIndex = constructedName.length();
            }
            constructedName.insert(identifierIndex, "-" + api.getIdentifier());
            outputFileName = constructedName.toString();
        }
        return outputFileName;
    }

    static class Arguments {
        @Parameter
        List<String> inputs = Lists.newArrayList();

        @Parameter(names = { "-o", "--out" }, description = "File to write HTML documentation to")
        String outputFileName = "web-service-api.html";

        @Parameter(names = { "--exclude" }, description = "Endpoint pattern to exclude from the generated docs")
        List<String> excludes = Lists.newArrayList();

        @Parameter(names = { "-f", "--format" }, description = "Format for output: html or raml")
        String outputFormat = "html";
        
        @Parameter(names = { "-s", "--scope" }, description = "Publication scope for output (e.g. public, private, etc) or \"all\"")
        String scope = "all";
    }
}
