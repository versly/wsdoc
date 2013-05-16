package org.versly.rest.wsdoc;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class JaxRSRestAnnotationTypes implements AnnotationProcessor.RestAnnotationTypes {
    @Override
    public Class<? extends Annotation> getMappingAnnotationType() {
        return Path.class;
    }

    @Override
    public String getRequestPath(ExecutableElement executableElement, TypeElement contextClass) {
        Path anno = executableElement.getAnnotation(Path.class);
        if (anno == null)
            throw new IllegalStateException(String.format(
                    "The Path annotation for %s.%s is not parseable. Exactly one value is required.",
                    contextClass.getQualifiedName(), executableElement.getSimpleName()));
        else
            return anno.value();
    }

    @Override
    public String getRequestPath(TypeElement cls) {
        Path clsAnno = cls.getAnnotation(Path.class);
        if (clsAnno == null)
            return null;
        else
            return clsAnno.value();
    }

    @Override
    public String getRequestMethod(ExecutableElement executableElement, TypeElement contextClass) {
        List<String> methods = new ArrayList<String>();

        gatherMethod(executableElement, methods, GET.class);
        gatherMethod(executableElement, methods, PUT.class);
        gatherMethod(executableElement, methods, POST.class);
        gatherMethod(executableElement, methods, DELETE.class);

        if (methods.size() != 1)
            throw new IllegalStateException(String.format(
                "The method annotation for %s.%s is not parseable. Exactly one request method (GET/POST/PUT/DELETE) is required. Found: %s",
                contextClass.getQualifiedName(), executableElement.getSimpleName(), methods));

        return methods.get(0);
    }

    private void gatherMethod(ExecutableElement executableElement, List<String> methods, Class<? extends Annotation> anno) {
        if (executableElement.getAnnotation(anno) != null) {
            methods.add(anno.getSimpleName());
        }
    }

    @Override
    public String getPathVariable(VariableElement var) {
        PathParam param = var.getAnnotation(PathParam.class);
        return param == null ? null : param.value();
    }

    @Override
    public String getRequestParam(VariableElement var) {
        QueryParam param = var.getAnnotation(QueryParam.class);
        return param == null ? null : param.value();
    }
}
