package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Define this annotation on the method or on a class with "internal", to skip documenting it.
 * 
 * @author aisac
 *
 */
@Target(ElementType.TYPE)
public @interface DocumentationScope {
	
	String[] value() default {};
}
