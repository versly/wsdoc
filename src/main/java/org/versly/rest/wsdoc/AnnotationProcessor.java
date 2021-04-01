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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor6;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.versly.rest.wsdoc.impl.JaxRSRestImplementationSupport;
import org.versly.rest.wsdoc.impl.JsonArray;
import org.versly.rest.wsdoc.impl.JsonDict;
import org.versly.rest.wsdoc.impl.JsonObject;
import org.versly.rest.wsdoc.impl.JsonPrimitive;
import org.versly.rest.wsdoc.impl.JsonRecursiveObject;
import org.versly.rest.wsdoc.impl.JsonType;
import org.versly.rest.wsdoc.impl.RestDocumentation;
import org.versly.rest.wsdoc.impl.SpringMVC43RestImplementationSupport;
import org.versly.rest.wsdoc.impl.Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

/**
 * Generates an HTML documentation file describing the REST / JSON endpoints as defined with the
 * Spring {@link org.springframework.web.bind.annotation.RequestMapping} annotation. Outputs to <code>rest-api.html</code> in the top of the classes directory.
 */
// TODO:
//   - @CookieValue
//   - @RequestHeader
//   - @ResponseStatus
//   - combine class-level and method-level annotations properly
//   - MethodNameResolver
//   - plural RequestMapping value support (i.e., two paths bound to one method)
//   - support for methods not marked with @RequestMapping whose class does have a @RequestMapping annotation
@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.RequestMapping", "org.springframework.web.bind.annotation.GetMapping",
                           "org.springframework.web.bind.annotation.PostMapping", "org.springframework.web.bind.annotation.PatchMapping",
                           "org.springframework.web.bind.annotation.DeleteMapping", "org.springframework.web.bind.annotation.PutMapping",
                           "javax.ws.rs.Path", "javax.ws.rs.GET", "javax.ws.rs.PUT", "javax.ws.rs.POST", "javax.ws.rs.DELETE", "javax.ws.rs.HEAD", "javax.ws.rs.OPTIONS"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AnnotationProcessor extends AbstractProcessor {

    private RestDocumentation _docs = new RestDocumentation();
    private boolean _isComplete = false;
    private Map<TypeMirror, JsonType> _memoizedTypeMirrors = new HashMap<TypeMirror, JsonType>();
    private Map<DeclaredType, JsonType> _memoizedDeclaredTypes = new HashMap<DeclaredType, JsonType>();
    private ProcessingEnvironment _processingEnv;
    private Types _typeUtils;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        _processingEnv = processingEnv;
        _typeUtils = _processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> supportedAnnotations, RoundEnvironment roundEnvironment) {

        // short-circuit if there are multiple rounds
        if (_isComplete)
            return true;

        Collection<String> processedPackageNames = new LinkedHashSet<String>();
        processElements(roundEnvironment, processedPackageNames, new JaxRSRestImplementationSupport());
        processElements(roundEnvironment, processedPackageNames, new SpringMVC43RestImplementationSupport());
        _docs.postProcess();

        if (_docs.getApis().size() > 0) {

            OutputStream fileOutput = null;
            try {
                FileObject file = getOutputFile();
                boolean exists = new File(file.getName()).exists();
                fileOutput = file.openOutputStream();
                _docs.toStream(fileOutput);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        String.format("Wrote REST docs for %s apis to %s file at %s",
                                _docs.getApis().size(), exists ? "existing" : "new", file.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e); // TODO wrap in something nicer
            } finally {
                if (fileOutput != null) {
                    try {
                        fileOutput.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                }
            }
        }
        _isComplete = true;
        return true;
    }

    private void processElements(RoundEnvironment roundEnvironment,
                                 Collection<String> processedPackageNames,
                                 RestImplementationSupport implementationSupport) {

        for (Class<? extends Annotation> a : implementationSupport.getExtendedMappingAnnotationTypes()) {
            for (Element e : roundEnvironment.getElementsAnnotatedWith(a)) {

                if (e instanceof ExecutableElement) {
                    addPackageName(processedPackageNames, e);
                    processRequestMappingMethod((ExecutableElement) e, implementationSupport);
                }
            }
        }
    }

    private void addPackageName(Collection<String> processedPackageNames, Element e) {
        processedPackageNames.add(processingEnv.getElementUtils().getPackageOf(e).getQualifiedName().toString());
    }

    private FileObject getOutputFile() throws IOException {
        return this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", Utils.SERIALIZED_RESOURCE_LOCATION);
    }

    private void processRequestMappingMethod(ExecutableElement executableElement, RestImplementationSupport implementationSupport) {
        TypeElement cls = (TypeElement) executableElement.getEnclosingElement();

        for (final String basePath : getClassLevelUrlPaths(cls, implementationSupport)) {
            for (final String requestPath : implementationSupport.getRequestPaths(executableElement, cls)) {
                String fullPath = Utils.joinPaths(basePath, requestPath);
                String meth;
                try {
                    meth = implementationSupport.getRequestMethod(executableElement, cls);
                } catch (IllegalStateException ex) {
                    // if something is bad with the request method annotations (no PATCH support currently, for example),
                    // then just warn and continue, so the docs don't break the dev process.
                    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                            "error processing element: " + ex.getMessage(), executableElement);
                    continue;
                }

                // both spring and jersey permit colon delimited regexes in path annotations which are not compatible with RAML
                // which expects resource identifiers to comply with RFC-6570 URI template semantics - so remove regex portion
                String[] splitedPath = fullPath.split("/");
                if (splitedPath != null && splitedPath.length > 0) {
                    for (int splitedPathIndex = 0; splitedPathIndex < splitedPath.length; splitedPathIndex++) {
                        splitedPath[splitedPathIndex] = splitedPath[splitedPathIndex].replaceAll(":.*}", "}");
                        if (splitedPathIndex==1 && splitedPath[splitedPathIndex] != null) {
                            // According to RFC-6570 URI template semantics, 1st parameter cannot be a variable
                            // Spring programs can have a variable as 1st parameter e.g. "${spring.application.name}/api/v1"
                            splitedPath[splitedPathIndex] = splitedPath[splitedPathIndex].replaceAll("[{}]", "");
                        }
                    }
                    fullPath = String.join("/", splitedPath);
                }

                // set documentation and metadata on api
                RestDocumentation.RestApi api = null;
                DocumentationRestApi apidoc = cls.getAnnotation(DocumentationRestApi.class);
                if (null != apidoc) {
                    api = _docs.getRestApi(apidoc.id());
                    api.setApiTitle(apidoc.title());
                    api.setApiVersion(apidoc.version());
                    api.setMount(apidoc.mount());
                }
                else {
                    api = _docs.getRestApi(RestDocumentation.RestApi.DEFAULT_IDENTIFIER);
                    api.setApiTitle("");
                    api.setApiVersion("");
                    api.setMount("");
                }

                api.setApiDocumentation(processingEnv.getElementUtils().getDocComment(cls));

                // set documentation text on method
                RestDocumentation.RestApi.Resource resource = api.getResourceDocumentation(fullPath); 
                RestDocumentation.RestApi.Resource.Method method = resource.newMethodDocumentation(meth);
                method.setCommentText(processingEnv.getElementUtils().getDocComment(executableElement));

                // set documentation scope on method (doc scope is non-scalar, methods can be part of multiple doc scopes)
                {
                    HashSet<String> docScopes = new HashSet<String>();
                    DocumentationScope clsDocScopes = cls.getAnnotation(DocumentationScope.class);
                    if (null != clsDocScopes) {
                        docScopes.addAll(Arrays.asList(clsDocScopes.value()));
                    }
                    DocumentationScope methodDocScopes = executableElement.getAnnotation(DocumentationScope.class);
                    if (null != methodDocScopes) {
                        docScopes.addAll(Arrays.asList(methodDocScopes.value()));
                    }
                    method.setDocScopes(docScopes);
                }
                
                // set authorization scope on method (auth scope is non-scalar, methods can have multiple auth scopes)
                {
                    HashSet<String> authScopes = new HashSet<String>();
                    AuthorizationScope methodAuthScopes = executableElement.getAnnotation(AuthorizationScope.class);
                    if (null != methodAuthScopes) {
                        authScopes.addAll(Arrays.asList(methodAuthScopes.value()));
                    }
                    else {
                        AuthorizationScope clsAuthScopes = cls.getAnnotation(AuthorizationScope.class);
                        if (null != clsAuthScopes) {
                            authScopes.addAll(Arrays.asList(clsAuthScopes.value()));
                        }
                    }
                    method.setAuthScopes(authScopes);
                }

                // set traits on method (traits is non-scalar, methods may have multiple traits)
                {
                    HashSet<String> traits = new HashSet<String>(method.getDocScopes());
                    DocumentationTraits methodTraits = executableElement.getAnnotation(DocumentationTraits.class);
                    if (null != methodTraits) {
                        traits.addAll(Arrays.asList(methodTraits.value()));
                    }
                    else {
                        DocumentationTraits clsTraits = cls.getAnnotation(DocumentationTraits.class);
                        if (null != clsTraits) {
                            traits.addAll(Arrays.asList(clsTraits.value()));
                        }
                    }
                    method.setTraits(traits);
                }
                
                // set response object if annotated
                TypeMirror type = getTypeFromAnnotation(executableElement);

                // add method's traits as included with overall API traits (used in RAML for uniform documentation)
                api.getTraits().addAll(method.getTraits());
                
                // set path and query parameter information on method
                buildParameterData(executableElement, method, implementationSupport);
                
                // set response entity data information on method
                buildResponseFormat(type, method);
            }
        }
    }

    private static TypeMirror getTypeFromAnnotation(final ExecutableElement executableElement) {
        ReturnType returnType = executableElement.getAnnotation(ReturnType.class);
        TypeMirror type = executableElement.getReturnType();
        if (null != returnType) {
            try {
                returnType.value();
            } catch (MirroredTypeException mte) {
                type = mte.getTypeMirror();
            }

            if (!executableElement.getReturnType().equals(type)
                    && (!ModelAndView.class.getName().equals(executableElement.getReturnType().toString())
                            && !Response.class.getName().equals(executableElement.getReturnType().toString()))) {
                throw new UnsupportedOperationException(String.format(
                        "@ReturnType annotated class [%s] cannot be used on methods that do not return generic responses; actual response must be either [%s] or [%s] but was [%s]",
                        type, ModelAndView.class.getName(), Response.class.getName(),
                        executableElement.getReturnType()));
            }
        }
        return type;
    }

    private void buildParameterData(ExecutableElement executableElement, RestDocumentation.RestApi.Resource.Method doc,
                                    RestImplementationSupport implementationSupport) {

        // only process @RequestBody, @PathVariable and @RequestParam parameters for now.
        // TODO Consider expanding this to include other Spring REST annotations.

        // We can safely ignore @RequestMapping.params, as Spring requires that a @RequestParam exists
        // for each entry listed in this list. I expect that this might be the same for @RequestMapping.headers

        scanForSpringMVCMultipart(executableElement, doc);
        buildPathVariables(executableElement, doc, implementationSupport);
        buildUrlParameters(executableElement, doc, implementationSupport);
        buildPojoQueryParameters(executableElement, doc, implementationSupport);
        buildRequestBodies(executableElement, doc, implementationSupport);
    }

    /**
     * This is Spring-MVC only -- JAX-RS doesn't have an (obvious) analog.
     */
    private void scanForSpringMVCMultipart(ExecutableElement executableElement, RestDocumentation.RestApi.Resource.Method doc) {
        for (VariableElement var : executableElement.getParameters()) {
            TypeMirror varType = var.asType();
            if (varType.toString().startsWith(MultipartHttpServletRequest.class.getName())) {
                doc.setMultipartRequest(true);
                return;
            }
        }
    }

    private void buildRequestBodies(ExecutableElement executableElement, RestDocumentation.RestApi.Resource.Method doc,
                                    RestImplementationSupport implementationSupport) {
        List<VariableElement> requestBodies = new ArrayList<VariableElement>();
        for (VariableElement var : executableElement.getParameters()) {
            if (implementationSupport.isRequestBody(var))
                requestBodies.add(var);
        }

        if (requestBodies.size() > 1)
            throw new IllegalStateException(String.format(
                    "Method %s in class %s has multiple @RequestBody params",
                    executableElement.getSimpleName(), executableElement.getEnclosingElement()));

        if (requestBodies.size() == 1)
            buildRequestBody(requestBodies.get(0), doc);
    }

    private void buildRequestBody(VariableElement var, RestDocumentation.RestApi.Resource.Method doc) {
        doc.setRequestBody(jsonTypeFromTypeMirror(var.asType(), new HashSet<String>()));
        doc.setRequestSchema(jsonSchemaFromTypeMirror(var.asType()));
        doc.setRequestExample(exampleFromJsonType(doc.getRequestBody()));
    }

    private void buildPathVariables(ExecutableElement executableElement, RestDocumentation.RestApi.Resource.Method doc,
                                    RestImplementationSupport implementationSupport) {
        RestDocumentation.RestApi.Resource.UrlFields subs = doc.getUrlSubstitutions();

        for (VariableElement var : executableElement.getParameters()) {
            String pathVariable = implementationSupport.getPathVariable(var);
            if (pathVariable != null) {
                String paramName = var.getSimpleName().toString();
                addUrlField(subs, var, pathVariable, findParamDescription(paramName, doc.getCommentText()));
            }
        }
    }

    private void addUrlField(RestDocumentation.RestApi.Resource.UrlFields subs, VariableElement var, String annoValue,
            String description) {
        String name = (annoValue == null || annoValue.isEmpty()) ? var.getSimpleName().toString() : annoValue;
        subs.addField(name, jsonTypeFromTypeMirror(var.asType(), new HashSet<String>()), description);
    }

    private void buildUrlParameters(ExecutableElement executableElement, RestDocumentation.RestApi.Resource.Method doc,
                                    RestImplementationSupport implementationSupport) {
        RestDocumentation.RestApi.Resource.UrlFields subs = doc.getUrlParameters();

        for (VariableElement var : executableElement.getParameters()) {
            String reqParam = implementationSupport.getRequestParam(var);
            if (reqParam != null) {
                String paramName = var.getSimpleName().toString();
                addUrlField(subs, var, reqParam, findParamDescription(paramName, doc.getCommentText()));
            }
        }
    }

    String findParamDescription(String paramName, String methodJavaDoc)
    {
        String desc = null;
        if (methodJavaDoc != null && StringUtils.isNotEmpty(paramName)) {
            String token = "@param " + paramName;
            int startIndex = methodJavaDoc.indexOf(token);
            if (startIndex != -1) {
                int endIndex = methodJavaDoc.indexOf("@param", startIndex + 1);
                if (endIndex == -1) {
                    endIndex = methodJavaDoc.indexOf("@return", startIndex + 1);
                }
                if (endIndex != -1) {
                    desc = methodJavaDoc.substring(startIndex + token.length(), endIndex);
                } else {
                    desc = methodJavaDoc.substring(startIndex + token.length());
                }
                desc = fixCommentWhitespace(desc);
            }
        }

        return desc;
    }

    private String fixCommentWhitespace(String desc) {
        return desc == null ? null : StringUtils.strip(desc.replace("\n", " ").replace("\r", " ").replaceAll(" {2,}", " "));
    }

    /**
     * Finds any request parameters that can be bound to (which are pojos) and adds each of the POJOs fields to the url parameters
     */
    private void buildPojoQueryParameters(ExecutableElement executableElement, RestDocumentation.RestApi.Resource.Method doc,
                                          RestImplementationSupport implementationSupport) {
        if (doc.getRequestMethod().equals(RequestMethod.GET.name())) {
            RestDocumentation.RestApi.Resource.UrlFields subs = doc.getUrlParameters();
            for (VariableElement var : executableElement.getParameters()) {
                if (implementationSupport.getPojoRequestParam(var) != null) {
                    Element paramType = _typeUtils.asElement(var.asType());
                    List<ExecutableElement> methods = ElementFilter.methodsIn(paramType.getEnclosedElements());
                    for (ExecutableElement method : methods) {
                        if (method.getSimpleName().toString().startsWith("set") && method.getParameters().size() == 1) {
                            String setterComment = processingEnv.getElementUtils().getDocComment(method);
                            TypeMirror setterType = method.getParameters().get(0).asType();
                            JsonType jsonType = jsonTypeFromTypeMirror(setterType, new HashSet<String>());
                            String propName = StringUtils.uncapitalize(method.getSimpleName().toString().substring(3));
                            subs.addField(propName, jsonType, fixCommentWhitespace(setterComment));
                        }
                    }
                }
            }
        }

    }

    private JsonType jsonTypeFromTypeMirror(TypeMirror typeMirror, Collection<String> typeRecursionGuard) {

        JsonType type;

        if (_memoizedTypeMirrors.containsKey(typeMirror)) {
            return _memoizedTypeMirrors.get(typeMirror);
        }

        if (isJsonPrimitive(typeMirror)) {
            type = new JsonPrimitive(typeMirror.toString());
        } else if (typeMirror.getKind() == TypeKind.DECLARED) {
            // some sort of object... walk it
            DeclaredType declaredType = (DeclaredType) typeMirror;
            type = jsonTypeForDeclaredType(declaredType, declaredType.getTypeArguments(), typeRecursionGuard);
        } else if (typeMirror.getKind() == TypeKind.VOID) {
            type = null;
        } else if (typeMirror.getKind() == TypeKind.ARRAY) {
            TypeMirror componentType = ((ArrayType) typeMirror).getComponentType();
            type = jsonTypeFromTypeMirror(componentType, typeRecursionGuard);
        } else if (typeMirror.getKind() == TypeKind.ERROR) {
            type = new JsonPrimitive("(unresolvable type)");
        } 
        else {
            throw new UnsupportedOperationException(typeMirror.toString());
        }

        _memoizedTypeMirrors.put(typeMirror, type);
        return type;
    }

    /**
     * Return a JSON type for the given declared type. The caller is responsible for
     * providing a list of concrete types to use to replace parameterized type placeholders.
     */
    private JsonType jsonTypeForDeclaredType(DeclaredType type, List<? extends TypeMirror> concreteTypes,
                                             Collection<String> typeRecursionGuard) {

        JsonType jt = _memoizedDeclaredTypes.get(type);
        if (jt == null) {
            TypeVisitorImpl visitor = new TypeVisitorImpl(type, concreteTypes, typeRecursionGuard);
            jt = type.accept(visitor, null);
            _memoizedDeclaredTypes.put(type, jt);
        }
        return jt;
    }

    private boolean isJsonPrimitive(TypeMirror typeMirror) {
        return (typeMirror.getKind().isPrimitive()
        || JsonPrimitive.isPrimitive(typeMirror.toString()));
    }

    private void buildResponseFormat(TypeMirror type, RestDocumentation.RestApi.Resource.Method doc) {
        type = convertAsyncResponseTypes(type);
        doc.setResponseBody(jsonTypeFromTypeMirror(type, new HashSet<String>()));
        doc.setResponseSchema(jsonSchemaFromTypeMirror(type));
        doc.setResponseExample(exampleFromJsonType(doc.getResponseBody()));
    }

    private TypeMirror convertAsyncResponseTypes(TypeMirror type) {
        if (type.toString().startsWith(WebAsyncTask.class.getName())) {
            return ((DeclaredType) type).getTypeArguments().get(0);
        } else {
            return type;
        }
    }

    private String[] getClassLevelUrlPaths(TypeElement cls, RestImplementationSupport implementationSupport) {

        String basePath = null;
        DocumentationRestApi api = cls.getAnnotation(DocumentationRestApi.class);
        RestApiMountPoint mp = cls.getAnnotation(RestApiMountPoint.class);
        if (null != api) {
            basePath = api.mount();
        }
        else if (null != mp) {
            basePath = mp.value();
        }
        else {
            basePath = "/";
        }

        String[] paths = implementationSupport.getRequestPaths(cls);
        if (paths.length == 0) {
            return new String[] { basePath };
        } else {
            for (int i = 0; i < paths.length; i++) {
                paths[i] = Utils.joinPaths(basePath, paths[i]);
            }
            return paths;
        }
    }

    private class TypeVisitorImpl extends AbstractTypeVisitor6<JsonType,Void> {
        private Map<Name, DeclaredType> _typeArguments = new HashMap<Name, DeclaredType>();
        private Collection<String> _typeRecursionDetector;
        private DeclaredType _type;

        public TypeVisitorImpl(DeclaredType type, List<? extends TypeMirror> typeArguments,
                               Collection<String> typeRecursionGuard) {

            _typeRecursionDetector = typeRecursionGuard;
            _type = type;
            loadTypeElements(type, typeArguments);
        }

        private void loadTypeElements(DeclaredType type, List<? extends TypeMirror> typeArguments) {
            // TODO test this with generic interfaces, including in superclasses. Issue #10.

            TypeElement elem = (TypeElement) type.asElement();
            if (Object.class.getName().equals(elem.getQualifiedName().toString()))
                return;

            if (elem.getSuperclass() instanceof DeclaredType) {
                DeclaredType sup = (DeclaredType) elem.getSuperclass();
                loadTypeElements(sup, sup.getTypeArguments());
            }

            List<? extends TypeParameterElement> generics = elem.getTypeParameters();
            for (int i = 0; i < generics.size(); i++) {
                DeclaredType value =
                        (typeArguments.isEmpty() || !(typeArguments.get(i) instanceof DeclaredType)) ?
                                null : (DeclaredType) typeArguments.get(i);
                _typeArguments.put(generics.get(i).getSimpleName(), value);
            }

        }

        @Override
        public JsonType visitPrimitive(PrimitiveType primitiveType, Void o) {
            return jsonTypeFromTypeMirror(primitiveType, new HashSet<String>(_typeRecursionDetector));
        }

        @Override
        public JsonType visitNull(NullType nullType, Void o) {
            throw new UnsupportedOperationException(nullType.toString());
        }

        @Override
        public JsonType visitArray(ArrayType arrayType, Void o) {
            throw new UnsupportedOperationException(arrayType.toString());
        }

        @Override
        public JsonType visitDeclared(DeclaredType declaredType, Void o) {
            // Transparently replace Optional<T> with T. This is specifically
            // to support Jackson's Optional handling implemented in Jdk8Module.
            if (isInstanceOf(declaredType, Optional.class)) {
                TypeParameterElement elem = ((TypeElement) declaredType.asElement()).getTypeParameters().get(0);
                return visit(elem.asType());
            }

            if (_typeRecursionDetector.contains(declaredType.toString()))
                return new JsonRecursiveObject(declaredType.asElement().getSimpleName().toString());

            if (isJsonPrimitive(declaredType)) {
                // 'primitive'-ish things
                return new JsonPrimitive(declaredType.toString());

            } else if (isInstanceOf(declaredType, Collection.class)) {

                if (declaredType.getTypeArguments().size() == 0) {
                    return new JsonArray(new JsonPrimitive(Object.class.getName()));
                } else {
                    TypeParameterElement elem = ((TypeElement) declaredType.asElement()).getTypeParameters().get(0);

                    _typeRecursionDetector.add(declaredType.toString());
                    return new JsonArray(acceptOrRecurse(o, elem.asType()));
                }

            } else if (isInstanceOf(declaredType, Map.class)) {

                if (declaredType.getTypeArguments().size() == 0) {
                    return new JsonDict(
                            new JsonPrimitive(Object.class.getName()), new JsonPrimitive(Object.class.getName()));
                } else {
                    TypeMirror key = declaredType.getTypeArguments().get(0);
                    TypeMirror val = declaredType.getTypeArguments().get(1);

                    _typeRecursionDetector.add(declaredType.toString());
                    JsonType keyJson = acceptOrRecurse(o, key);
                    JsonType valJson = acceptOrRecurse(o, val);
                    return new JsonDict(keyJson, valJson);
                }

            } else {
                TypeElement element = (TypeElement) declaredType.asElement();
                if (element.getKind() == ElementKind.ENUM) {
                    List<String> enumConstants = new ArrayList<String>();
                    for (Element e : element.getEnclosedElements()) {
                        if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                            enumConstants.add(e.toString());
                        }
                    }
                    JsonPrimitive primitive = new JsonPrimitive(String.class.getName()); // TODO is this always a string?
                    primitive.setRestrictions(enumConstants);
                    return primitive;
                } else {
                    return buildType(declaredType, element);
                }
            }
        }

        private JsonType acceptOrRecurse(Void o, TypeMirror type) {
            return type instanceof DeclaredType ? recurseForJsonType((DeclaredType) type) : type.accept(this, o);
        }

        private JsonType buildType(DeclaredType declaredType, TypeElement element) {
            if (_typeRecursionDetector.contains(declaredType.toString()))
                return new JsonRecursiveObject(element.getSimpleName().toString());

            JsonObject json = new JsonObject();
            buildTypeContents(json, element);
            return json; // we've already added to the cache; short-circuit to handle recursion
        }

        private boolean isInstanceOf(TypeMirror typeMirror, Class type) {
            if (!(typeMirror instanceof DeclaredType))
                return false;

            if (typeMirror.toString().startsWith(type.getName()))
                return true;

            DeclaredType declaredType = (DeclaredType) typeMirror;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            for (TypeMirror iface : typeElement.getInterfaces()) {
                if (isInstanceOf(iface, type))
                    return true;
            }
            return isInstanceOf(typeElement.getSuperclass(), type);
        }

        private void buildTypeContents(JsonObject o, TypeElement element) {
            // Spring-MVC and JAX-RS both support methods that return a builder object
            // that contains the real underlying response payload. These should not be
            // expressed as response values.
            if (ModelAndView.class.getName().equals(element.getQualifiedName().toString())) {
                return;
            }
            if (Response.class.getName().equals(element.getQualifiedName().toString())) {
                return;
            }

            if (element.getSuperclass().getKind() != TypeKind.NONE) {
                // an interface's superclass is TypeKind.NONE

                DeclaredType sup = (DeclaredType) element.getSuperclass();
                if (!isJsonPrimitive(sup))
                    buildTypeContents(o, (TypeElement) sup.asElement());
            }

            for (Element e : element.getEnclosedElements()) {
                if (e instanceof ExecutableElement) {
                    addFieldFromBeanMethod(o, (ExecutableElement) e);
                }
            }
        }

        private void addFieldFromBeanMethod(JsonObject o, ExecutableElement executableElement) {
            if (!isJsonBeanGetter(executableElement))
                return;

            TypeMirror type = executableElement.getReturnType();
            String methodName = executableElement.getSimpleName().toString();
            int trimLength = methodName.startsWith("is") ? 2 : 3;

            // if the name is something trivial like 'get', skip it. See issue #15.
            if (methodName.length() <= trimLength) {
                return;
            }

            String beanName = methodName.substring(trimLength + 1, methodName.length());
            beanName = methodName.substring(trimLength, trimLength + 1).toLowerCase() + beanName;

            // replace variables with the current concrete manifestation
            if (type instanceof TypeVariable) {
                type = getDeclaredTypeForTypeVariable((TypeVariable) type);
                if (type == null)
                    return; // couldn't find a replacement -- must be a generics-capable type with no generics info
            }

            String docComment = processingEnv.getElementUtils().getDocComment(executableElement);
            if (type instanceof DeclaredType) {
                JsonType jsonType = recurseForJsonType((DeclaredType) type);
                o.addField(beanName, jsonType)
                        .setCommentText(docComment);
            } else {
                o.addField(beanName, jsonTypeFromTypeMirror(type, new HashSet<String>(_typeRecursionDetector)))
                        .setCommentText(docComment);
            }
        }

        private JsonType recurseForJsonType(DeclaredType type) {
            // loop over the element's generic types, and build a concrete list from the owning context
            List<DeclaredType> concreteTypes = new ArrayList<DeclaredType>();

            for (TypeMirror generic : type.getTypeArguments()) {
                if (generic instanceof DeclaredType) {
                    concreteTypes.add((DeclaredType) generic);
                } else if (generic instanceof TypeVariable) {
                    concreteTypes.add(_typeArguments.get(((TypeVariable) generic).asElement().getSimpleName()));
                } else {
                    _processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        String.format(
                            "wsdoc encountered an unsupported generics construct while processing type %s. Generic: %s",
                            type, generic));
                }
            }
            _typeRecursionDetector.add(_type.toString());
            Collection<String> types = new HashSet<String>(_typeRecursionDetector);
            return jsonTypeForDeclaredType(type, concreteTypes, types);
        }

        private boolean isJsonBeanGetter(ExecutableElement executableElement) {
            if (executableElement.getKind() != ElementKind.METHOD)
                return false;

            if (executableElement.getReturnType().getKind() == TypeKind.NULL)
                return false;

            if (!(executableElement.getSimpleName().toString().startsWith("get")
            || executableElement.getSimpleName().toString().startsWith("is")))
                return false;

            if (executableElement.getParameters().size() > 0)
                return false;

            return executableElement.getAnnotation(JsonIgnore.class) == null;
        }

        @Override
        public JsonType visitError(ErrorType errorType, Void o) {
            throw new UnsupportedOperationException(errorType.toString());
        }

        @Override
        public JsonType visitTypeVariable(TypeVariable typeVariable, Void o) {
            DeclaredType type = getDeclaredTypeForTypeVariable(typeVariable);
            if (type != null) { // null: un-parameterized usage of a generics-having type
                try {
                    return type.accept(this, o);
                } catch (UnsupportedOperationException e) {
                    // likely we ran into a type we can't work with (e.g. ErrorType), continue with best effort
                    return null;
                }
            }
            else {
                return null;
            }
        }

        private DeclaredType getDeclaredTypeForTypeVariable(TypeVariable typeVariable) {
            Name name = typeVariable.asElement().getSimpleName();
            if (!_typeArguments.containsKey(name)) {
                throw new UnsupportedOperationException(String.format(
                        "Unknown parameterized type: %s. Available types in this context: %s.",
                        typeVariable.toString(), _typeArguments));
            } else {
                return _typeArguments.get(name);
            }
        }

        @Override
        public JsonType visitWildcard(WildcardType wildcardType, Void o) {
            throw new UnsupportedOperationException(wildcardType.toString());
        }

        @Override
        public JsonType visitExecutable(ExecutableType executableType, Void o) {
            throw new UnsupportedOperationException(executableType.toString());
        }

        @Override
        public JsonType visitNoType(NoType noType, Void o) {
            throw new UnsupportedOperationException(noType.toString());
        }

        @Override
        public JsonType visitUnknown(TypeMirror typeMirror, Void o) {
            throw new UnsupportedOperationException(typeMirror.toString());
        }
    }

    public interface RestImplementationSupport {
        Class<? extends Annotation> getMappingAnnotationType();

        Set<Class<? extends Annotation>> getExtendedMappingAnnotationTypes();

        String[] getRequestPaths(ExecutableElement executableElement, TypeElement contextClass);

        String[] getRequestPaths(TypeElement cls);

        String getRequestMethod(ExecutableElement executableElement, TypeElement contextClass);

        String getPathVariable(VariableElement var);

        String getRequestParam(VariableElement var);

        String getPojoRequestParam(VariableElement var);

        boolean isRequestBody(VariableElement var);
    }

    String exampleFromJsonType(JsonType type) {
        return renderJson(type);
    }

    String renderJson(JsonType type) {
        String schema = "";
        if (type instanceof org.versly.rest.wsdoc.impl.JsonPrimitive) {
            schema = schema + renderJsonPrimitive((JsonPrimitive) type, null);
        } else if (type instanceof org.versly.rest.wsdoc.impl.JsonObject) {
            schema = schema + renderJsonObject((JsonObject) type);
        } else if (type instanceof org.versly.rest.wsdoc.impl.JsonRecursiveObject){
            schema = schema + renderJsonRecursiveObject((JsonRecursiveObject) type);
        } else if (type instanceof org.versly.rest.wsdoc.impl.JsonArray) {
            schema = schema + renderJsonArray((JsonArray) type);
        } else if (type instanceof org.versly.rest.wsdoc.impl.JsonDict) {
            schema = schema + renderJsonDict((JsonDict) type);
        }
        return schema;
    }

    private String renderJsonArray(JsonArray type) {
        return "[" + renderJson(type.getElementType()) + "]";
    }

    private String renderJsonDict(JsonDict type) {
        return "{" + renderJson(type.getKeyType()) + ": " + renderJson(type.getValueType()) + " }";
    }

    private String renderJsonPrimitive(JsonPrimitive type, String comment) {
        String primStr = "\"" + type.getTypeName();
        if (!CollectionUtils.isEmpty(type.getRestrictions())) {
            primStr = primStr + " one of [" + join(type.getRestrictions().toArray(), ",") + "]";
        }
        if (comment != null) {
            primStr = primStr + " /* " + comment + " */";
        }
        return primStr + "\"";
    }

    private String renderJsonObject(JsonObject type) {
        String objStr = "{";
        Iterator<JsonObject.JsonField> fieldIt = type.getFields().iterator();
        while (fieldIt.hasNext()) {
            JsonObject.JsonField jsonField = fieldIt.next();
            objStr = objStr + "\"" + jsonField.getFieldName() + "\": ";
            if (jsonField.getFieldType() instanceof JsonPrimitive) {
                objStr = objStr + renderJsonPrimitive((JsonPrimitive) jsonField.getFieldType(), getComment(jsonField));
            } else {
                objStr = objStr + renderJson(jsonField.getFieldType());
            }
            if (fieldIt.hasNext()) {
                objStr = objStr + ",";
            }
        }
        return objStr + "}";
    }

    private String getComment(JsonObject.JsonField jsonField) {
        String comment = null;
        if (isNotEmpty(jsonField.getCommentText())) {
            comment =  jsonField.getCommentText().replaceAll("(\r|\n)+| {2,}", " ");
        }
        return comment;
    }

    private String renderJsonRecursiveObject(JsonRecursiveObject type) {
        return "\"" + type.getRecursedObjectTypeName() + " recursive\"";
    }

    String jsonSchemaFromTypeMirror(TypeMirror type) {
        String serializedSchema = null;

        if (type.getKind().isPrimitive() || type.getKind() == TypeKind.VOID) {
            return null;
        }

        // we need the dto class to generate schema using jackson json-schema module
        // note: Types.erasure() provides canonical names whereas Class.forName() wants a "regular" name,
        // so forName will fail for nested and inner classes as "regular" names use $ between parent and child.
        Class dtoClass = null;
        StringBuffer erasure = new StringBuffer(_typeUtils.erasure(type).toString());
        for (boolean done = false; !done; ) {
            try {
                dtoClass = Class.forName(erasure.toString());
                done = true;
            } catch (ClassNotFoundException e) {
                if (erasure.lastIndexOf(".") != -1) {
                    erasure.setCharAt(erasure.lastIndexOf("."), '$');
                }
                else
                {
                    done = true;
                }
            }
        }

        // if we were able to figure out the dto class, use jackson json-schema module to serialize it
        Exception e = null;
        if (dtoClass != null) {
            try {
                ObjectMapper m = new ObjectMapper();
                m.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
                m.registerModule(new JodaModule());
                SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
                m.acceptJsonFormatVisitor(m.constructType(dtoClass), visitor);
                serializedSchema = m.writeValueAsString(visitor.finalSchema());
            } catch (Exception ex) {
                e = ex;
            }
        }

        // report warning if we were not able to generate schema for non-primitive type
        if (serializedSchema == null)
        {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "cannot generate json-schema for class " + type.toString() + " (erasure " + erasure + "), " +
                            ((e != null) ? ("exception: " + e.getMessage()) : "class not found"));
        }

        return serializedSchema;
    }
}
