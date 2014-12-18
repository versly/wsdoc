package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Define this annotation on the method or on a class with Scope.Internal, to skip documenting it.
 * 
 * @author aisac
 *
 */
@Target(ElementType.TYPE)
public @interface DocumentationScope {
	
	public enum Scope {
		Internal,	//Internal, will not include in API documentation
		External	//Will include it in documentation.
	}
	
	Scope value() default Scope.External;
}
