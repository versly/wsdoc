package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Documents the expected return type for wrapped responses.
 * Example: @ReturnType(User.class) public Response getUser() {...}
 */
@Target(ElementType.METHOD)
public @interface ReturnType {
    Class<?> value();
}
