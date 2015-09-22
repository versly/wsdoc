package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface DocumentationRestApi {
    String ID_TEMPLATE = "@WSDOC_ID@";
    String MOUNT_TEMPLATE = "@WSDOC_MOUNT@";
    String TITLE_TEMPLATE = "@WSDOC_TITLE@";
    String VERSION_TEMPLATE = "@WSDOC_VERSION@";

    String id() default "(default)";
    String mount() default "";
    String title() default "";
    String version() default "";
}
