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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.RequestMethod;

public class RestDocumentation implements Serializable {

    private Map<String, Resource> _resources = new LinkedHashMap();

    public Collection<Resource> getResources() {
        return _resources.values();
    }

    public Resource getResourceDocumentation(String path) {
        if (!_resources.containsKey(path))
            _resources.put(path, new Resource(path));
        return _resources.get(path);
    }

    /**
     * Read and return a serialized {@link RestDocumentation} instance from <code>in</code>,
     * as serialized by {@link #toStream}.
     */
    public static RestDocumentation fromStream(InputStream in)
        throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(in);
            return (RestDocumentation) ois.readObject();
        } finally {
            if (ois != null)
                ois.close();
        }
    }

    public void toStream(OutputStream out) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(this);
        oos.flush();
    }

    public RestDocumentation filter(Iterable<Pattern> excludePatterns) {
        RestDocumentation filtered = new RestDocumentation();
        OUTER: for (Map.Entry<String, Resource> entry : _resources.entrySet()) {
            for (Pattern excludePattern : excludePatterns)
                if (excludePattern.matcher(entry.getKey()).matches())
                    continue OUTER;

            filtered._resources.put(entry.getKey(), entry.getValue());
        }
        return filtered;
    }

    public class Resource implements Serializable {

        private String path;
        private Collection<Method> _methods = new LinkedList<Method>();

        public Resource(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public Collection<Method> getRequestMethodDocs() {
            return _methods;
        }

        /**
         * Creates and returns a new {@link Method} instance, and adds it to
         * the current resource location. If this is invoked multiple times
         * with the same argument, multiple distinct {@link Method} objects
         * will be returned.
         */
        public Method newMethodDocumentation(RequestMethod meth) {
            Method method = new Method(meth);
            _methods.add(method);
            return method;
        }

        public class Method implements Serializable {

            private RequestMethod meth;
            private JsonType _requestBody;
            private UrlFields _urlSubstitutions = new UrlFields();
            private UrlFields _urlParameters = new UrlFields();
            private JsonType _responseBody;
            private String _commentText;
            private boolean _isMultipartRequest;
            private UrlFields queryParameters = new UrlFields();

            public Method(RequestMethod meth) {
                this.meth = meth;
            }

            public RequestMethod getRequestMethod() {
                return meth;
            }

            public JsonType getRequestBody() {
                return _requestBody;
            }

            public void setRequestBody(JsonType body) {
                _requestBody = body;
            }

            public UrlFields getUrlSubstitutions() {
                return _urlSubstitutions;
            }

            public UrlFields getUrlParameters() {
                return _urlParameters;
            }

            public JsonType getResponseBody() {
                return _responseBody;
            }

            public void setResponseBody(JsonType body) {
                _responseBody = body;
            }

            public String getCommentText() {
                return _commentText;
            }

            public void setCommentText(String text) {
                _commentText = text;
            }

            public boolean isMultipartRequest() {
                return _isMultipartRequest;
            }

            public void setMultipartRequest(boolean multipart) {
                _isMultipartRequest = multipart;
            }
            
            public void setQueryParameters(UrlFields queryParams) {
                this.queryParameters = queryParams;
            }
            
            public UrlFields getQueryParameters() {
                return queryParameters;
            }

            /**
             * An HTML-safe, textual key that uniquely identifies this endpoint.
             */
            public String getKey() {
                String key = path + "_" + meth.name();
                for (String param : _urlParameters.getFields().keySet()) {
                    key += "_" + param;
                }
                return key;
            }

            public class UrlFields implements Serializable {

                private Map<String, JsonType> _jsonTypes = new LinkedHashMap();

                public Map<String, JsonType> getFields() {
                    return _jsonTypes;
                }

                public void addField(String name, JsonType jsonType) {
                    _jsonTypes.put(name, jsonType);
                }
            }

        }
    }
}
