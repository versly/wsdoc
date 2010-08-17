/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public class Resource implements Serializable {

        private String path;
        private Map<RequestMethod, Method> _methods = new LinkedHashMap();

        public Resource(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public Collection<Method> getRequestMethodDocs() {
            return _methods.values();
        }

        public Method getMethodDocumentation(RequestMethod meth) {
            if (!_methods.containsKey(meth))
                _methods.put(meth, new Method(meth));
            return _methods.get(meth);
        }

        public class Method implements Serializable {

            private RequestMethod meth;
            private JsonType _requestBody;
            private UrlSubstitutions _urlSubstitutions = new UrlSubstitutions();
            private JsonType _responseBody;

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

            public Method.UrlSubstitutions getUrlSubstitutions() {
                return _urlSubstitutions;
            }

            public JsonType getResponseBody() {
                return _responseBody;
            }

            public void setResponseBody(JsonType body) {
                _responseBody = body;
            }

            public class UrlSubstitutions implements Serializable {

                private Map<String, JsonType> _jsonTypes = new LinkedHashMap();

                public Map<String, JsonType> getSubstitutions() {
                    return _jsonTypes;
                }

                public void addSubstitution(String pathSubName, JsonType jsonType) {
                    _jsonTypes.put(pathSubName, jsonType);
                }
            }
        }
    }
}
