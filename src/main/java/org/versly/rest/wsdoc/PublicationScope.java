package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Define this annotation on the method or on a class with "internal", to skip documenting it.
 * 
 * @author aisac
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface PublicationScope {

	public static final String PUBLIC = "public";
	public static final String PRIVATE = "private";

	String[] value() default {};
}
