/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.versly.rest.wsdoc;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Generates an HTML documentation file describing the REST / JSON endpoints as defined with the
 * Spring {@link org.springframework.web.bind.annotation.RequestMapping} annotation. Outputs to
 * <code>rest-api.html</code> in the top of the classes directory.
 */
// TODO:
//   - @CookieValue
//   - @RequestHeader
//   - @ResponseStatus
//   - combine class-level and method-level annotations properly
//   - MethodNameResolver
//   - plural RequestMapping value support (i.e., two paths bound to one method)
//   - support for methods not marked with @RequestMapping whose class does have a @RequestMapping annotation
@SupportedAnnotationTypes("org.springframework.web.bind.annotation.RequestMapping")
public class AnnotationProcessor extends AbstractProcessor {

    RestDocumentation docs = new RestDocumentation();
    private boolean _isComplete = false;

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

        if (docs.getResources().size() > 0) {

            OutputStream fout = null;
            try {
                FileObject file = getOutputFile();
                boolean exists = new File(file.getName()).exists();
                fout = file.openOutputStream();
                docs.toStream(fout);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                    String.format("Wrote REST docs for %s endpoints to %s file at %s", 
                        docs.getResources().size(), exists ? "existing" : "new", file.getName()));
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
        System.out.println("Processing request mapping method: " + executableElement.toString());
        TypeElement cls = (TypeElement) executableElement.getEnclosingElement();
        String path = getClassLevelUrlPath(cls);

        RequestMapping anno = executableElement.getAnnotation(RequestMapping.class);
        path = addMethodPathComponent(executableElement, cls, path, anno);
        RequestMethod meth = getRequestMethod(executableElement, cls, anno);

        RestDocumentation.Resource.Method doc = docs.getResourceDocumentation(path).newMethodDocumentation(meth);
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
        doc.setRequestBody(newJsonType(var.asType()));
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
        subs.addField(name, newJsonType(var.asType()));
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

    private JsonType newJsonType(TypeMirror typeMirror) {
        if (isJsonPrimitive(typeMirror)) {
            return new JsonPrimitive(typeMirror.toString());
        } else if (typeMirror.getKind() == TypeKind.DECLARED) {
            // some sort of object... walk it
            DeclaredType type = (DeclaredType) typeMirror;
            return newJsonType(type, type.getTypeArguments());
        } else if (typeMirror.getKind() == TypeKind.VOID) {
            return null;
        } else if (typeMirror.getKind() == TypeKind.ARRAY) {
            return newJsonType(((ArrayType)typeMirror).getComponentType());
        } else {
            throw new UnsupportedOperationException(typeMirror.toString());
        }
    }

    /**
     * Create a new JSON type for the given declared type. The caller is responsible for
     * providing a list of concrete types to use to replace parameterized type placeholders.
     */
    private JsonType newJsonType(DeclaredType type, List<? extends TypeMirror> concreteTypes) {
        TypeVisitorImpl visitor = new TypeVisitorImpl((TypeElement) type.asElement(), concreteTypes);
        return type.accept(visitor, null);
    }

    private boolean isJsonPrimitive(TypeMirror typeMirror) {
        return (typeMirror.getKind().isPrimitive()
            || JsonPrimitive.isPrimitive(typeMirror.toString()));
    }

    private void buildResponseFormat(TypeMirror type, RestDocumentation.Resource.Method doc) {
        doc.setResponseBody(newJsonType(type));
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

    private class TypeVisitorImpl implements TypeVisitor<JsonType,Void> {
        private Map<Name, DeclaredType> _typeArguments = new HashMap();

        public TypeVisitorImpl(TypeElement type, List<? extends TypeMirror> typeArguments) {
            List<? extends TypeParameterElement> generics = type.getTypeParameters();
            for (int i = 0; i < generics.size(); i++) {
                DeclaredType value =
                        (typeArguments.isEmpty() || !(typeArguments.get(i) instanceof DeclaredType)) ?
                                null : (DeclaredType) typeArguments.get(i);
                _typeArguments.put(generics.get(i).getSimpleName(), value);
            }
        }

        @Override
        public JsonType visit(TypeMirror typeMirror, Void o) {
            throw new UnsupportedOperationException(typeMirror.toString());
        }

        @Override
        public JsonType visit(TypeMirror typeMirror) {
            throw new UnsupportedOperationException(typeMirror.toString());
        }

        @Override
        public JsonType visitPrimitive(PrimitiveType primitiveType, Void o) {
            return newJsonType(primitiveType);
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
                }

                TypeMirror elem = declaredType.getTypeArguments().get(0);
                return new JsonArray(elem.accept(this, o));
                
            } else if (isInstanceOf(declaredType, Map.class)) {

                if (declaredType.getTypeArguments().size() == 0) {
                    return new JsonDict(
                        new JsonPrimitive(Object.class.getName()), new JsonPrimitive(Object.class.getName()));
                }

                TypeMirror key = declaredType.getTypeArguments().get(0);
                TypeMirror val = declaredType.getTypeArguments().get(1);
                return new JsonDict(key.accept(this, o), val.accept(this, o));

            } else {
                TypeElement element = (TypeElement) declaredType.asElement();
                if (element.getKind() == ElementKind.ENUM) {
                    List<String> enumConstants = new ArrayList();
                    for (Element e : element.getEnclosedElements()) {
                        if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                            enumConstants.add(e.toString());
                        }
                    }
                    JsonPrimitive primitive = new JsonPrimitive(String.class.getName());  // TODO is this always a string?
                    primitive.setRestrictions(enumConstants);
                    return primitive;
                } else {
                    return buildType(element);
                }
            }
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

        private JsonObject buildType(TypeElement element) {
            JsonObject json = new JsonObject();
            buildTypeContents(json, element);
            return json;
        }

        private void buildTypeContents(JsonObject o, TypeElement element) {
            if ("org.springframework.web.servlet.ModelAndView".equals(element.getQualifiedName().toString())) {
                return;
            }
            DeclaredType sup = (DeclaredType) element.getSuperclass();
            if (!isJsonPrimitive(sup))
                buildTypeContents(o, (TypeElement) sup.asElement());

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

            // loop over the element's generic types, and build a concrete list from the owning context
            List<DeclaredType> concreteTypes = new ArrayList();

            // replace variables with the current concrete manifestation
            if (type instanceof TypeVariable) {
                type = getDeclaredTypeForTypeVariable((TypeVariable) type);
                if (type == null)
                    return; // couldn't find a replacement -- must be a generics-capable type with no generics info
            }

            String docComment = processingEnv.getElementUtils().getDocComment(executableElement);
            if (type instanceof DeclaredType) {
                TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
                for (TypeParameterElement generic : element.getTypeParameters()) {
                    concreteTypes.add(_typeArguments.get(generic.getSimpleName()));
                }
                o.addField(beanName, newJsonType((DeclaredType) type, concreteTypes))
                    .setCommentText(docComment);
            } else {
                o.addField(beanName, newJsonType(type))
                    .setCommentText(docComment);
            }
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
