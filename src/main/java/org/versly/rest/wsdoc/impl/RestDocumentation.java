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

package org.versly.rest.wsdoc.impl;

import org.apache.commons.lang3.StringUtils;
import org.versly.rest.wsdoc.DocumentationRestApi;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class RestDocumentation implements Serializable {
    private static final long serialVersionUID = -3416760457544073264L;
    public static final String DEFAULT_API = "default";

    private Map<String, RestApi> _apis = new LinkedHashMap();

    public RestApi getRestApi(String apiBaseUrl) {
        if (!_apis.containsKey(apiBaseUrl))
            _apis.put(apiBaseUrl, new RestApi(apiBaseUrl));
        return _apis.get(apiBaseUrl);
    }

    public Collection<RestApi> getApis() {
        return _apis.values();
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

    /**
     * This inspects the method paths and establishes parent/child relationships.  This helps in particular
     * with generating RAML documentation, as RAML represents endpoints hierarchically.
     */
    public void postProcess() {
        for (RestApi api : _apis.values()) {
            String mount = api.getMount();
            if (null != mount && mount.length() > 0) {
                api.getResourceDocumentation(api.getMount());
            }
            for (RestApi.Resource visitor : api.getResources()) {
                for (RestApi.Resource visitee : api.getResources()) {
                    if (visitee != visitor && visitee.path.startsWith(visitor.path + "/") &&
                            (visitee._parent == null || visitee._parent.path.length() < visitor.path.length())) {
                        if (visitee._parent != null) {
                            visitee._parent._children.remove(visitee);
                        }
                        visitee._parent = visitor;
                        visitor._children.add(visitee);
                    }
                }
            }
        }
    }
    
    public class RestApi implements Serializable {
        private static final long serialVersionUID = 5665219205108618731L;
        public static final String DEFAULT_IDENTIFIER = "(default)";
        
        private Map<String, Resource> _resources = new LinkedHashMap();
        private String _identifier;
        private String _apiMount;
        private String _apiTitle;
        private String _apiVersion;
        private String _apiDocumentation;
        private HashSet<String> _traits = new HashSet<String>();

        public RestApi(String identifier) {
            _identifier = identifier;
        }

        public Object readResolve() throws ObjectStreamException {
            _identifier = Utils.fillTemplate(_identifier);
            _apiMount = Utils.fillTemplate(_apiMount);
            _apiTitle = Utils.fillTemplate(_apiTitle);
            _apiVersion = Utils.fillTemplate(_apiVersion);
            return this;
        }

        public String getIdentifier() {
            return _identifier;
        }

        public String getMount() {
            return _apiMount;
        }

        public void setMount(String apiBaseUrl) {
            if (null != apiBaseUrl && !apiBaseUrl.trim().isEmpty()) {
                _apiMount = apiBaseUrl;
            }
        }

        public String getApiTitle() {
            return _apiTitle;
        }

        public void setApiTitle(String apiTitle) {
            if (null != apiTitle && !apiTitle.trim().isEmpty()) {
                _apiTitle = apiTitle;
            }
        }

        public String getApiVersion() {
            return _apiVersion;
        }

        public void setApiVersion(String apiVersion) {
            if (null != apiVersion && !apiVersion.trim().isEmpty()) {
                _apiVersion = apiVersion;
            }
        }

        public String getApiDocumentation() {
            
            return _apiDocumentation;
        }

        public void setApiDocumentation(String apiDocumentation) {
            if (null != apiDocumentation && !apiDocumentation.trim().isEmpty()) {
                _apiDocumentation = apiDocumentation;
            }
        }

        public String getIndentedApiDocumentationText(int indent) {
            if (_apiDocumentation != null) {
                String whitespace = StringUtils.leftPad("", indent);
                return whitespace + _apiDocumentation.replaceAll("\n", "\n" + whitespace);
            }
            return "";
        }

        public HashSet<String> getTraits() {
            return _traits;
        }

        public void setTraits(HashSet<String> traits) {
            this._traits = traits;
        }

        public String getIndentedApiTraits(int indent) {
            StringBuilder retval = new StringBuilder();
            for (String trait : _traits) {
                retval.append(StringUtils.leftPad("", indent));
                retval.append("- ");
                retval.append(trait);
                retval.append(":\n");
                retval.append(StringUtils.leftPad("", 2*indent));
                retval.append("description: TBD\n");
            }
            return retval.toString();
        }
    
        public Collection<Resource> getResources() {
            return _resources.values();
        }

        public void merge(RestApi api) {
            if (null == _apiTitle || _apiTitle.trim().isEmpty()) {
                _apiTitle = api.getApiTitle();
            }
            if (null == _apiVersion || _apiVersion.trim().isEmpty()) {
                _apiVersion = api.getApiVersion();
            }
            if (null == _apiMount || _apiMount.trim().isEmpty()) {
                _apiMount = api.getMount();
            }
            if (null == _apiDocumentation || _apiDocumentation.trim().isEmpty()) {
                _apiDocumentation = api.getApiDocumentation();
            }
            _resources.putAll(api._resources);
            _traits.addAll(api._traits);
        }
        
        public Resource getResourceDocumentation(String path) {
            if (!_resources.containsKey(path))
                _resources.put(path, new Resource(path));
            return _resources.get(path);
        }

        public RestApi filter(Iterable<Pattern> excludePatterns) {
            RestApi filtered = new RestApi(_identifier);
            filtered.setMount(_apiMount);
            filtered.setApiTitle(_apiTitle);
            filtered.setApiVersion(_apiVersion);
            filtered.setApiDocumentation(_apiDocumentation);
            filtered.setTraits(_traits);
            OUTER:
            for (Map.Entry<String, Resource> entry : _resources.entrySet()) {
                for (Pattern excludePattern : excludePatterns)
                    if (excludePattern.matcher(entry.getKey()).matches())
                        continue OUTER;

                filtered._resources.put(entry.getKey(), entry.getValue());
            }
            return filtered;
        }
        
        public class Resource implements Serializable {
            private static final long serialVersionUID = -3436348850301436626L;

            private String path;
            private Map<String, Method> _methods = new LinkedHashMap();
            private Resource _parent;
            private Collection<Resource> _children = new LinkedList<Resource>();

            public Resource(String path) {
                this.path = path;
            }

            public String getPath() {
                return path;
            }

            public Collection<Method> getRequestMethodDocs() {
                return _methods.values();
            }

            public Resource getParent() {
                return _parent;
            }

            ;

            public String getPathLeaf() {
                return (_parent != null) ? path.substring(_parent.path.length()) : path;
            }

            public Collection<Resource> getChildren() {
                return _children;
            }

            /**
             * Creates and returns a new {@link Method} instance, and adds it to
             * the current resource location. If this is invoked multiple times
             * with the same argument, the same {@link Method} object
             * will be returned.
             */
            public Method newMethodDocumentation(String meth) {
                if (_methods.containsKey(meth)) {
                    return _methods.get(meth);
                }
                Method method = new Method(meth);
                _methods.put(meth, method);
                return method;
            }

            public UrlFields getResourceUrlSubstitutions() {
                UrlFields aggregateUrlFields = new UrlFields();
                for (Method method : _methods.values()) {
                    UrlFields fields = method.getMethodSpecificUrlSubstitutions();
                    aggregateUrlFields.getFields().putAll(fields.getFields());
                }
                return aggregateUrlFields;
            }

            private Object readResolve() throws ObjectStreamException {
                path = Utils.fillTemplate(path);
                return this;
            }

            public class Method implements Serializable {

                private String _meth;
                private HashSet<String> _docScopes;
                private HashSet<String> _traits;
                private HashSet<String> _authScopes;
                private JsonType _requestBody;
                private UrlFields _urlSubstitutions = new UrlFields();
                private UrlFields _urlParameters = new UrlFields();
                private JsonType _responseBody;
                private String _commentText;
                private boolean _isMultipartRequest;
                private boolean _isWebsocket;
                private String _requestSchema;
                private String _responseSchema;
                private String _responseExample;
                private String _requestExample;

                public HashSet<String> getDocScopes() {
                    return _docScopes;
                }

                public void setDocScopes(HashSet<String> scopes) {
                    this._docScopes = scopes;
                }

                public HashSet<String> getTraits() {
                    return _traits;
                }

                public String getTraitsAsString() {
                    StringBuilder sb = new StringBuilder("[ ");
                    for (String trait : _traits) {
                        sb.append(trait).append(",");
                    }
                    sb.setCharAt(sb.length() - 1, ']');
                    return sb.toString();
                }

                public void setTraits(HashSet<String> traits) {
                    this._traits = traits;
                }

                public HashSet<String> getAuthScopes() {
                    return _authScopes;
                }

                public String getAuthScopesAsString() {
                    if (null != _authScopes && _authScopes.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[");
                        for (String authScope : _authScopes) {
                            sb.append("\"");
                            sb.append(authScope);
                            sb.append("\",");
                        }
                        if (sb.length() > 1) {
                            sb.setCharAt(sb.length() - 1, ']');
                        }
                        else {
                            sb.append("]");
                        }
                        return sb.toString();
                    }
                    return null;
                }
                
                public void setAuthScopes(HashSet<String> _authScopes) {
                    this._authScopes = _authScopes;
                }

                public String getResponseSchema() {
                    return _responseSchema;
                }

                public void setResponseSchema(String _responseSchema) {
                    this._responseSchema = _responseSchema;
                }

                public String getRequestSchema() {
                    return _requestSchema;
                }

                public void setRequestSchema(String _requestSchema) {
                    this._requestSchema = _requestSchema;
                }

                public void setResponseExample(String wsDocResponseSchema) {
                    this._responseExample = wsDocResponseSchema;
                }

                public String getResponseExample() {
                    return _responseExample;
                }

                public void setRequestExample(String wsDocRequestSchema) {
                    this._requestExample = wsDocRequestSchema;
                }

                public String getRequestExample() {
                    return _requestExample;
                }

                public Method(String meth) {
                    this._meth = meth;
                }

                public String getRequestMethod() {
                    return _meth;
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

                /**
                 * Get the URI parameters specific to this method (useful in RAML where the parent hierarchy will already include it's own)
                 *
                 * @return
                 */
                public UrlFields getMethodSpecificUrlSubstitutions() {
                    Resource parent = _parent;
                    Map<String, UrlFields.UrlField> methodFields = new HashMap<String, UrlFields.UrlField>(_urlSubstitutions.getFields());
                    while (parent != null) {
                        Iterator<Method> iter = parent.getRequestMethodDocs().iterator();
                        while (iter.hasNext()) {
                            for (String key : iter.next()._urlSubstitutions.getFields().keySet()) {
                                methodFields.remove(key);
                            }
                        }
                        parent = parent._parent;
                    }
                    UrlFields urlFields = new UrlFields();
                    urlFields.getFields().putAll(methodFields);
                    return urlFields;
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

                public String getIndentedCommentText(int indent) {
                    if (_commentText != null) {
                        String whitespace = StringUtils.leftPad("", indent);
                        return whitespace + _commentText.split("\n @")[0].replaceAll("\n", "\n" + whitespace);
                    }
                    return null;
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
                
                public boolean isWebsocket() {
                    return _isWebsocket;
                }

                public void setWebsocket(boolean websocket) {
                    _isWebsocket = websocket;
                }

                /**
                 * An HTML-safe, textual key that uniquely identifies this endpoint.
                 */
                public String getKey() {
                    String key = path + "_" + _meth;
                    for (String param : _urlParameters.getFields().keySet()) {
                        key += "_" + param;
                    }
                    return key;
                }
            }

            public class UrlFields implements Serializable {

                private Map<String, UrlField> _jsonFields = new LinkedHashMap();

                public class UrlField implements Serializable {

                    private JsonType fieldType;
                    private String fieldDescription;

                    public UrlField(JsonType type, String desc) {
                        fieldType = type;
                        fieldDescription = desc;
                    }

                    public JsonType getFieldType() {
                        return fieldType;
                    }

                    public void setFieldType(JsonType fieldType) {
                        this.fieldType = fieldType;
                    }

                    public String getFieldDescription() {
                        return fieldDescription;
                    }

                    public void setFieldDescription(String fieldDescription) {
                        this.fieldDescription = fieldDescription;
                    }
                }

                public Map<String, UrlField> getFields() {
                    return _jsonFields;
                }

                public void addField(String name, JsonType jsonType, String description) {
                    _jsonFields.put(name, new UrlField(jsonType, description));
                }
            }
        }
    }
}
