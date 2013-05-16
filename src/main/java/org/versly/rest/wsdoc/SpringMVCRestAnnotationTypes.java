package org.versly.rest.wsdoc;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;

class SpringMVCRestAnnotationTypes implements AnnotationProcessor.RestAnnotationTypes {

    @Override
    public Class<? extends Annotation> getMappingAnnotationType() {
        return RequestMapping.class;
    }

    @Override
    public String getRequestPath(ExecutableElement executableElement, TypeElement contextClass) {
        RequestMapping anno = executableElement.getAnnotation(RequestMapping.class);
        if (anno == null || anno.value().length != 1)
            throw new IllegalStateException(String.format(
                    "The RequestMapping annotation for %s.%s is not parseable. Exactly one value is required.",
                    contextClass.getQualifiedName(), executableElement.getSimpleName()));
        else
            return anno.value()[0];
    }

    @Override
    public String getRequestPath(TypeElement cls) {
        RequestMapping clsAnno = cls.getAnnotation(RequestMapping.class);
        if (clsAnno == null || clsAnno.value().length == 0)
            return null;
        else if (clsAnno.value().length == 1)
            return clsAnno.value()[0];
        else
            throw new IllegalStateException(String.format(
                    "The RequestMapping annotation of class %s has multiple value strings. Only zero or one value is supported",
                    cls.getQualifiedName()));
    }

    @Override
    public String getRequestMethod(ExecutableElement executableElement, TypeElement contextClass) {
        RequestMapping anno = executableElement.getAnnotation(RequestMapping.class);
        if (anno.method().length != 1)
            throw new IllegalStateException(String.format(
                    "The RequestMapping annotation for %s.%s is not parseable. Exactly one request method (GET/POST/etc) is required.",
                    contextClass.getQualifiedName(), executableElement.getSimpleName()));
        else
            return anno.method()[0].name();
    }

    @Override
    public String getPathVariable(VariableElement var) {
        PathVariable pathVar = var.getAnnotation(PathVariable.class);
        return pathVar == null ? null : pathVar.value();
    }

    @Override
    public String getRequestParam(VariableElement var) {
        RequestParam reqParam = var.getAnnotation(RequestParam.class);
        return reqParam == null ? null : reqParam.value();
    }
}
