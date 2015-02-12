package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Define a scope for an endpoint or its containing class.  The documentation of endpoints may be filtered
 * based on scope.
 * 
 * @author aisac
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface DocumentationScope {

	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";

	String[] value() default {};
}
