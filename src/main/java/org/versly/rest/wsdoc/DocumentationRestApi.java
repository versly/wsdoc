package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface DocumentationRestApi {
    String id() default "(default)";
    String title() default "";
    String version() default "";
}
