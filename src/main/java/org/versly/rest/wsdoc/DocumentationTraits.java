package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface DocumentationTraits {
    
    public static final String STABLE = "stable";
    public static final String DEPRECATED = "deprecated";
    public static final String EXPERIMENTAL = "experimental";

    String[] value() default {};
}
