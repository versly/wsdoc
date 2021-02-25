package org.versly.rest.wsdoc.impl;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Implements support for method-level composed-variants for
 * {@link org.springframework.web.bind.annotation.RequestMapping} annotation introduced since Spring Framework 4.3.
 *
 * @author Sidharth Mishra
 * @see <a href="https://docs.spring.io/spring-framework/docs/4.3.1.RELEASE/spring-framework-reference/htmlsingle/#mvc-ann-requestmapping-composed">mvc-ann-requestmapping-composed</a>
 */
public class SpringMVC43RestImplementationSupport extends SpringMVCRestImplementationSupport {

    /** The new method-level composed annotations introduced since Spring Framework 4.3. */
    private Set<Class<? extends Annotation>> getSpring43ComposedAnnotationTypes() {

        return new HashSet<>(Arrays.asList(GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class));
    }
    
    /**
     * Gets the path-value for the supported annotations.
     *
     * @param annotation the method-level composed annotations from Spring Framework 4.3+
     * @return the request-paths if the annotation is supported, else {@link Optional#empty()}.
     */
    private Optional<String[]> getRequestPathsFromAnnotation(Annotation annotation) {

        if (annotation instanceof GetMapping) {

            return Optional.of(((GetMapping) annotation).value());
        } else if (annotation instanceof PostMapping) {

            return Optional.of(((PostMapping) annotation).value());
        } else if (annotation instanceof PutMapping) {

            return Optional.of(((PutMapping) annotation).value());
        } else if (annotation instanceof PatchMapping) {

            return Optional.of(((PatchMapping) annotation).value());
        } else if (annotation instanceof DeleteMapping) {

            return Optional.of(((DeleteMapping) annotation).value());
        }

        return Optional.empty();
    }

    /**
     * Gets the HTTP-method name for the supported annotation.
     *
     * @param annotation the spring-mvc annotation,
     * @return the HTTP-method name for the annotation if supported, else {@link Optional#empty()}.
     */
    private Optional<String> getRequestMethod(Annotation annotation) {

        if (annotation instanceof GetMapping) {

            return Optional.of(GET.name());
        } else if (annotation instanceof PostMapping) {

            return Optional.of(POST.name());
        } else if (annotation instanceof PutMapping) {

            return Optional.of(PUT.name());
        } else if (annotation instanceof PatchMapping) {

            return Optional.of(PATCH.name());
        } else if (annotation instanceof DeleteMapping) {

            return Optional.of(DELETE.name());
        }

        return Optional.empty();
    }

    /**
     * Gets the request-paths for the supported Spring MVC method-level annotations.
     * Starts with the newer method-level composed annotations introduced since Spring Framework 4.3. If it cannot find any new annotations, delegates the control to super-class
     * for handling {@link RequestMapping} annotation.
     *
     * @param annotationProvider a function that searches for the annotation of the annotationClass' type on the element under process.
     * @param altSupplier        the supplier to use in-case the annotation is {@link RequestMapping} –– to be delegated to the super-class,
     * @return the request-paths from supported annotations, else an empty-string-array.
     */
    private String[] getRequestPaths(Function<Class<? extends Annotation>, Annotation> annotationProvider, Supplier<String[]> altSupplier) {

        for (Class<? extends Annotation> annotationClass : this.getSpring43ComposedAnnotationTypes()) {

            Annotation annotation = annotationProvider.apply(annotationClass);

            if (Objects.nonNull(annotation)) {

                Optional<String[]> requestPaths = getRequestPathsFromAnnotation(annotation);

                if (requestPaths.isPresent()) {

                    return requestPaths.get();
                }
            }
        }
        // Delegate the request-path computation for the RequestMapping annotated methods to super-class
        //
        return altSupplier.get();
    }

    /** Supports {@link RequestMapping} and the new method-level composed annotations introduced in Spring Framework 4.3. */
    @Override
    public Set<Class<? extends Annotation>> getExtendedMappingAnnotationTypes() {

        Set<Class<? extends Annotation>> supportedAnnotationTypes = new HashSet<>(Collections.singletonList(RequestMapping.class));

        supportedAnnotationTypes.addAll(this.getSpring43ComposedAnnotationTypes());

        return supportedAnnotationTypes;
    }

    @Override
    public String[] getRequestPaths(ExecutableElement executableElement, TypeElement contextClass) {

        return getRequestPaths(executableElement::getAnnotation, () -> super.getRequestPaths(executableElement, contextClass));
    }

    @Override
    public String[] getRequestPaths(TypeElement cls) {

        return getRequestPaths(cls::getAnnotation, () -> super.getRequestPaths(cls));
    }

    @Override
    public String getRequestMethod(ExecutableElement executableElement, TypeElement contextClass) {

        for (Class<? extends Annotation> annotationClass : this.getSpring43ComposedAnnotationTypes()) {

            Optional<String> requestMethod = getRequestMethod(executableElement.getAnnotation(annotationClass));

            if (requestMethod.isPresent()) {

                return requestMethod.get();
            }
        }
        // Delegate to super-class to handle the RequestMapping annotation
        //
        return super.getRequestMethod(executableElement, contextClass);
    }
}
