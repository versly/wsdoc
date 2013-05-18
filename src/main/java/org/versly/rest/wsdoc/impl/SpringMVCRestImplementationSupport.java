package org.versly.rest.wsdoc.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.versly.rest.wsdoc.AnnotationProcessor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;

public class SpringMVCRestImplementationSupport implements AnnotationProcessor.RestImplementationSupport {

    @Override
    public Class<? extends Annotation> getMappingAnnotationType() {
        return RequestMapping.class;
    }

    @Override
    public String[] getRequestPaths(ExecutableElement executableElement, TypeElement contextClass) {
        RequestMapping anno = executableElement.getAnnotation(RequestMapping.class);
        return requestPathsForAnnotation(anno);
    }

    @Override
    public String[] getRequestPaths(TypeElement cls) {
        RequestMapping clsAnno = cls.getAnnotation(RequestMapping.class);
        return requestPathsForAnnotation(clsAnno);
    }

    private String[] requestPathsForAnnotation(RequestMapping clsAnno) {
        if (clsAnno == null)
            return new String[0];
        else
            return clsAnno.value();
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
