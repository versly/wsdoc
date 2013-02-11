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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
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
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor6;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Generates an HTML documentation file describing the REST / JSON endpoints as defined with the
 * Spring {@link org.springframework.web.bind.annotation.RequestMapping} annotation. Outputs to <code>rest-api.html</code> in the top of the classes directory.
 */
// TODO:
// - @CookieValue
// - @RequestHeader
// - @ResponseStatus
// - combine class-level and method-level annotations properly
// - MethodNameResolver
// - plural RequestMapping value support (i.e., two paths bound to one method)
// - support for methods not marked with @RequestMapping whose class does have a @RequestMapping annotation
@SupportedAnnotationTypes("org.springframework.web.bind.annotation.RequestMapping")
public class AnnotationProcessor extends AbstractProcessor {

    private RestDocumentation _docs = new RestDocumentation();
    private boolean _isComplete = false;
    private Map<TypeMirror, JsonType> _memoizedTypeMirrors = new HashMap<TypeMirror, JsonType>();
    private Map<DeclaredType, JsonType> _memoizedDeclaredTypes = new HashMap<DeclaredType, JsonType>();

    @Override
    public boolean process(Set<? extends TypeElement> supportedAnnotations, RoundEnvironment roundEnvironment) {

        // short-circuit if there are multiple rounds
        if (_isComplete)
            return true;

        Collection<String> processedPackageNames = new LinkedHashSet<String>();
        for (Element e : roundEnvironment.getElementsAnnotatedWith(RequestMapping.class)) {
            if (e instanceof ExecutableElement) {
                addPackageName(processedPackageNames, e);
                processRequestMappingMethod((ExecutableElement) e);
            }
        }

        if (_docs.getResources().size() > 0) {

            OutputStream fout = null;
            try {
                FileObject file = getOutputFile();
                boolean exists = new File(file.getName()).exists();
                fout = file.openOutputStream();
                _docs.toStream(fout);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        String.format("Wrote REST docs for %s endpoints to %s file at %s",
                                _docs.getResources().size(), exists ? "existing" : "new", file.getName()));
            } catch (Exception e) {
                throw new RuntimeException(e); // TODO wrap in something nicer
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                }
            }
        }
        _isComplete = true;
        return true;
    }

    private void addPackageName(Collection<String> processedPackageNames, Element e) {
        processedPackageNames.add(processingEnv.getElementUtils().getPackageOf(e).getQualifiedName().toString());
    }

    private FileObject getOutputFile() throws IOException {
        return this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", Utils.SERIALIZED_RESOURCE_LOCATION);
    }

    private void processRequestMappingMethod(ExecutableElement executableElement) {
        TypeElement cls = (TypeElement) executableElement.getEnclosingElement();
        String path = getClassLevelUrlPath(cls);

        RequestMapping anno = executableElement.getAnnotation(RequestMapping.class);
        path = addMethodPathComponent(executableElement, cls, path, anno);
        RequestMethod meth = getRequestMethod(executableElement, cls, anno);

        RestDocumentation.Resource.Method doc = _docs.getResourceDocumentation(path).newMethodDocumentation(meth);
        doc.setCommentText(processingEnv.getElementUtils().getDocComment(executableElement));
        buildParameterData(executableElement, doc);
        buildResponseFormat(executableElement.getReturnType(), doc);
    }

    private void buildParameterData(ExecutableElement executableElement, RestDocumentation.Resource.Method doc) {

        // only process @RequestBody, @PathVariable and @RequestParam parameters for now.
        // TODO Consider expanding this to include other Spring REST annotations.

        // We can safely ignore @RequestMapping.params, as Spring requires that a @RequestParam exists
        // for each entry listed in this list. I expect that this might be the same for @RequestMapping.headers

        scanForMultipart(executableElement, doc);
        buildPathVariables(executableElement, doc);
        buildUrlParameters(executableElement, doc);
        buildQueryParameters(executableElement, doc);
        buildRequestBodies(executableElement, doc);
    }

    private void scanForMultipart(ExecutableElement executableElement, RestDocumentation.Resource.Method doc) {
        for (VariableElement var : executableElement.getParameters()) {
            TypeMirror varType = var.asType();
            if (varType.toString().startsWith(MultipartHttpServletRequest.class.getName())) {
                doc.setMultipartRequest(true);
                return;
            }
        }
    }

    private void buildRequestBodies(ExecutableElement executableElement, RestDocumentation.Resource.Method doc) {
        List<VariableElement> requestBodies = new ArrayList<VariableElement>();
        for (VariableElement var : executableElement.getParameters()) {
            if (var.getAnnotation(org.springframework.web.bind.annotation.RequestBody.class) != null)
                requestBodies.add(var);
        }

        if (requestBodies.size() > 1)
            throw new IllegalStateException(String.format(
                    "Method %s in class %s has multiple @RequestBody params",
                    executableElement.getSimpleName(), executableElement.getEnclosingElement()));

        if (requestBodies.size() == 1)
            buildRequestBody(requestBodies.get(0), doc);
    }

    private void buildRequestBody(VariableElement var, RestDocumentation.Resource.Method doc) {
        doc.setRequestBody(jsonTypeFromTypeMirror(var.asType(), new HashSet<DeclaredType>()));
    }

    private void buildPathVariables(ExecutableElement executableElement, RestDocumentation.Resource.Method doc) {
        RestDocumentation.Resource.Method.UrlFields subs = doc.getUrlSubstitutions();

        for (VariableElement var : executableElement.getParameters()) {
            PathVariable pathVar = var.getAnnotation(PathVariable.class);
            if (pathVar != null) {
                addUrlField(subs, var, pathVar.value());
            }
        }
    }

    private void addUrlField(RestDocumentation.Resource.Method.UrlFields subs, VariableElement var, String annoValue) {
        String name = (annoValue == null || annoValue.isEmpty()) ? var.getSimpleName().toString() : annoValue;
        subs.addField(name, jsonTypeFromTypeMirror(var.asType(), new HashSet<DeclaredType>()));
    }

    private void buildUrlParameters(ExecutableElement executableElement, RestDocumentation.Resource.Method doc) {
        RestDocumentation.Resource.Method.UrlFields subs = doc.getUrlParameters();

        for (VariableElement var : executableElement.getParameters()) {
            RequestParam reqParam = var.getAnnotation(RequestParam.class);
            if (reqParam != null) {
                addUrlField(subs, var, reqParam.value());
            }
        }
    }

    private void buildQueryParameters(ExecutableElement executableElement, RestDocumentation.Resource.Method doc) {
        if (doc.getRequestMethod().equals(RequestMethod.GET)) {
            for (VariableElement var : executableElement.getParameters()) {
                if (var.getAnnotation(RequestParam.class) == null && var.getAnnotation(PathVariable.class) == null) {
                    Element paramType = ((DeclaredType) var.asType()).asElement();
                    List<ExecutableElement> methods = ElementFilter.methodsIn(paramType.getEnclosedElements());
                    for (ExecutableElement method : methods) {
                        if (method.getSimpleName().toString().startsWith("set") && method.getParameters().size() == 1) {
                            TypeMirror setterType = method.getParameters().get(0).asType();
                            JsonType jsonType = jsonTypeFromTypeMirror(setterType, new ArrayList<DeclaredType>());
                            String propName = StringUtils.uncapitalize(method.getSimpleName().toString().substring(3));
                            doc.getQueryParameters().addField(propName, jsonType);
                        }
                    }
                }
            }
        }

    }

    private JsonType jsonTypeFromTypeMirror(TypeMirror typeMirror, Collection<DeclaredType> typeRecursionGuard) {

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
        } else {
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
            Collection<DeclaredType> typeRecursionGuard) {

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

    private void buildResponseFormat(TypeMirror type, RestDocumentation.Resource.Method doc) {
        doc.setResponseBody(jsonTypeFromTypeMirror(type, new HashSet<DeclaredType>()));
    }

    private RequestMethod getRequestMethod(ExecutableElement executableElement, TypeElement cls, RequestMapping anno) {
        if (anno.method().length != 1)
            throw new IllegalStateException(String.format(
                    "The RequestMapping annotation for %s.%s is not parseable. Exactly one request method (GET/POST/etc) is required.",
                    cls.getQualifiedName(), executableElement.getSimpleName()));
        else
            return anno.method()[0];
    }

    private String addMethodPathComponent(ExecutableElement executableElement, TypeElement cls, String path, RequestMapping anno) {
        if (anno == null || anno.value().length != 1)
            throw new IllegalStateException(String.format(
                    "The RequestMapping annotation for %s.%s is not parseable. Exactly one value is required.",
                    cls.getQualifiedName(), executableElement.getSimpleName()));
        else
            return Utils.joinPaths(path, anno.value()[0]);
    }

    private String getClassLevelUrlPath(TypeElement cls) {
        RestApiMountPoint mountPoint = cls.getAnnotation(RestApiMountPoint.class);
        String path = mountPoint == null ? "/" : mountPoint.value();

        RequestMapping clsAnno = cls.getAnnotation(RequestMapping.class);
        if (clsAnno == null || clsAnno.value().length == 0)
            return path;
        else if (clsAnno.value().length == 1)
            return Utils.joinPaths(path, clsAnno.value()[0]);
        else
            throw new IllegalStateException(String.format(
                    "The RequestMapping annotation of class %s has multiple value strings. Only zero or one value is supported",
                    cls.getQualifiedName()));
    }

    private class TypeVisitorImpl extends AbstractTypeVisitor6<JsonType, Void> {
        private Map<Name, DeclaredType> _typeArguments = new HashMap();
        private Collection<DeclaredType> _typeRecursionDetector;
        private DeclaredType _type;

        public TypeVisitorImpl(DeclaredType type, List<? extends TypeMirror> typeArguments,
                Collection<DeclaredType> typeRecursionGuard) {

            TypeElement elem = (TypeElement) type.asElement();
            _typeRecursionDetector = typeRecursionGuard;
            _type = type;
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
            return jsonTypeFromTypeMirror(primitiveType, new HashSet<DeclaredType>(_typeRecursionDetector));
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
            if (isJsonPrimitive(declaredType)) {
                // 'primitive'-ish things
                return new JsonPrimitive(declaredType.toString());

            } else if (isInstanceOf(declaredType, Collection.class)) {

                if (declaredType.getTypeArguments().size() == 0) {
                    return new JsonArray(new JsonPrimitive(Object.class.getName()));
                } else {
                    TypeMirror elem = declaredType.getTypeArguments().get(0);

                    _typeRecursionDetector.add(_type);
                    return new JsonArray(acceptOrRecurse(o, elem));
                }

            } else if (isInstanceOf(declaredType, Map.class)) {

                if (declaredType.getTypeArguments().size() == 0) {
                    return new JsonDict(
                            new JsonPrimitive(Object.class.getName()), new JsonPrimitive(Object.class.getName()));
                } else {
                    TypeMirror key = declaredType.getTypeArguments().get(0);
                    TypeMirror val = declaredType.getTypeArguments().get(1);

                    _typeRecursionDetector.add(_type);
                    JsonType keyJson = acceptOrRecurse(o, key);
                    JsonType valJson = acceptOrRecurse(o, val);
                    return new JsonDict(keyJson, valJson);
                }

            } else {
                TypeElement element = (TypeElement) declaredType.asElement();
                if (element.getKind() == ElementKind.ENUM) {
                    List<String> enumConstants = new ArrayList();
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

            if (_typeRecursionDetector.contains(declaredType))
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
            if ("org.springframework.web.servlet.ModelAndView".equals(element.getQualifiedName().toString())) {
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
                o.addField(beanName, jsonTypeFromTypeMirror(type, new HashSet<DeclaredType>(_typeRecursionDetector)))
                        .setCommentText(docComment);
            }
        }

        private JsonType recurseForJsonType(DeclaredType type) {
            // loop over the element's generic types, and build a concrete list from the owning context
            List<DeclaredType> concreteTypes = new ArrayList();

            TypeElement element = (TypeElement) type.asElement();
            for (TypeParameterElement generic : element.getTypeParameters()) {
                concreteTypes.add(_typeArguments.get(generic.getSimpleName()));
            }
            _typeRecursionDetector.add(_type);
            Collection<DeclaredType> types = new HashSet<DeclaredType>(_typeRecursionDetector);
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
            if (type != null) // null: un-parameterized usage of a generics-having type
                return type.accept(this, o);
            else
                return null;
        }

        private DeclaredType getDeclaredTypeForTypeVariable(TypeVariable typeVariable) {
            Name name = typeVariable.asElement().getSimpleName();
            if (!_typeArguments.containsKey(name)) {
                throw new UnsupportedOperationException(String.format(
                        "Unknown parameterized type: %s. Available types in this context: %s",
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
}
