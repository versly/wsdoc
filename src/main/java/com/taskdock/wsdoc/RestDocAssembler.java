/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class RestDocAssembler {

    public static void main(String... args) throws Exception {
        List<RestDocumentation> docs = new LinkedList<RestDocumentation>();
        for (String arg : args) {
            System.err.println("adding web service docs from " + arg);
            JarFile jar = new JarFile(arg);
            ZipEntry e = jar.getEntry("WEB-INF/classes/" + RestAnnotationProcessor.SERIALIZED_RESOURCE_LOCATION);
            ObjectInputStream ois = new ObjectInputStream(jar.getInputStream(e));
            docs.add((RestDocumentation) ois.readObject());
            ois.close();
            jar.close();
        }

        if (docs.size() > 0) {
            Configuration conf = new Configuration();
            conf.setClassForTemplateLoading(RestDocAssembler.class, "");
            conf.setObjectWrapper(new DefaultObjectWrapper());
            Writer out = null;
            try {
                Template template = conf.getTemplate("RestDocumentation.ftl");
                Map root = new HashMap();
                root.put("docs", docs);
                File file = getOutputFile();
                out = new FileWriter(file);
                template.process(root, out);
                out.flush();
                System.err.printf("Wrote REST docs to %s\n", file.getName());
            }
            finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                }
            }
        }
    }

    private static File getOutputFile() {
        return new File(System.getProperty("com.taskdock.wsdoc.outputFile", "web-service-api.html"));
    }
}
