/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import org.springframework.web.bind.annotation.RequestMethod;

import java.io.PrintStream;
import java.util.*;

public class RestDocumentation {

    private Map<String, Resource> _resources = new LinkedHashMap();

    public Resource getResourceDocumentation(String path) {
        if (!_resources.containsKey(path))
            _resources.put(path, new Resource(path));
        return _resources.get(path);
    }

    public void writePlainText(PrintStream stream) {
        for (Resource resource : _resources.values()) {
            resource.writePlainText(stream);
        }
    }

    public class Resource {

        private String path;
        private Map<RequestMethod, Method> _methods = new LinkedHashMap();

        public Resource(String path) {
            this.path = path;
        }

        public Method getMethodDocumentation(RequestMethod meth) {
            if (!_methods.containsKey(meth))
                _methods.put(meth, new Method(meth));
            return _methods.get(meth);
        }

        public void writePlainText(PrintStream stream) {
            for (Method meth : _methods.values()) {
                meth.writePlainText(stream);
            }
        }

        public class Method {

            private RequestMethod meth;
            private RequestBody _requestBodyDocumentation = new RequestBody();
            private UrlSubstitutions _urlSubstitutions = new UrlSubstitutions();
            private ResponseBody _responseBody = new ResponseBody();

            public Method(RequestMethod meth) {
                this.meth = meth;
            }

            public void writePlainText(PrintStream stream) {
                stream.printf("%s %s\n", meth, path);
                _requestBodyDocumentation.writePlainText(stream);
                _urlSubstitutions.writePlainText(stream);
                _responseBody.writePlainText(stream);
                stream.println();
            }

            public RequestBody getRequestBodyDocumentation() {
                return _requestBodyDocumentation;
            }

            public Method.UrlSubstitutions getUrlSubstitutions() {
                return _urlSubstitutions;
            }

            public Method.ResponseBody getResponseBody() {
                return _responseBody;
            }

            public class RequestBody {

                private JsonType _jsonType;

                public void writePlainText(PrintStream stream) {
                    if (_jsonType != null) {
                        stream.print("  Request Body Format:");
                        _jsonType.writePlainText(stream, 4);
                        stream.println();
                    }
                }

                public void setJsonValue(JsonType jsonType) {
                    _jsonType = jsonType;
                }
            }

            public class UrlSubstitutions {

                private Map<String, JsonType> _jsonValues = new LinkedHashMap();

                public void writePlainText(PrintStream stream) {
                    if (_jsonValues.size() != 0) {
                        stream.println("  URL Parameter Substitutions:");
                        for (Map.Entry<String, ? extends JsonType> entry : _jsonValues.entrySet()) {
                            stream.printf("    %s: ", entry.getKey());
                            entry.getValue().writePlainText(stream, 6);
                            stream.println();
                        }
                    }
                }

                public void addSubstitution(String pathSubName, JsonType jsonType) {
                    _jsonValues.put(pathSubName, jsonType);
                }
            }

            public class ResponseBody {

                private JsonType _jsonType;

                public void writePlainText(PrintStream stream) {
                    if (_jsonType != null) {
                        stream.print("  Response Body Format:");
                        _jsonType.writePlainText(stream, 4);
                        stream.println();
                    }
                }

                public void setJsonValue(JsonType jsonType) {
                    _jsonType = jsonType;
                }
            }
        }
    }
}
