package org.versly.rest.wsdoc.impl;

import java.lang.annotation.Annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.websocket.server.ServerEndpoint;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.versly.rest.wsdoc.AnnotationProcessor;

public class JavaEEWebsocketImplementationSupport  implements AnnotationProcessor.RestImplementationSupport {

    @Override
    public Class<? extends Annotation> getMappingAnnotationType() {
        return RequestMapping.class;
    }

    @Override
    public String[] getRequestPaths(ExecutableElement executableElement, TypeElement contextClass) {
        return new String[0];
    }

    @Override
    public String[] getRequestPaths(TypeElement cls) {
    	ServerEndpoint clsAnno = cls.getAnnotation(ServerEndpoint.class);
        return requestPathsForAnnotation(clsAnno);
    }

    private String[] requestPathsForAnnotation(ServerEndpoint clsAnno) {
        if (clsAnno == null)
            return new String[0];
        else
            return new String[]{ clsAnno.value() };
    }

    @Override
    public String getRequestMethod(ExecutableElement executableElement, TypeElement contextClass) {
        return "GET";
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

    /**
     * Return whether this variable is an un-annotated POJO.
     * This catches the case where a model is bound to directly. We explicitly exclude spring classes so that we don't look at the
     * magical spring bound parameters like Errors or ModelMap
     */
    @Override
    public String getPojoRequestParam(VariableElement var) {
        if (getRequestParam(var) == null && getPathVariable(var) == null && !ignorePojo(var)) {
            return ""; // No annotations to parse
        } else {
            return null;
        }

    }
    
    private boolean ignorePojo(VariableElement var)
    {
    	// Atmosphere Types
    	if(var.asType().toString().startsWith("org.atmosphere"))
    	{
    		return true;
    	}
    	
    	return false;
    }

    @Override
    public boolean isRequestBody(VariableElement var) {
    	return !var.asType().toString().equalsIgnoreCase("javax.websocket.Session");
    }
}