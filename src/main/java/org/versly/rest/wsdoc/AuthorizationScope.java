package org.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation documents Oauth2 authorization scopes, each of which satisfy the authorization scope criterion
 * for access to the annotated REST endpoint(s).  It may be applied at either the class or method level.  The
 * if applied at both levels, the effect is to recognize the union of scopes from both levels.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface AuthorizationScope {
    String[] value() default {};
}
