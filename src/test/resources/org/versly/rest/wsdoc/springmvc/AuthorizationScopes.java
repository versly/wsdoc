package org.versly.rest.wsdoc.springmvc; 

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.versly.rest.wsdoc.AuthorizationScope;
import org.versly.rest.wsdoc.RestApiMountPoint;

public class AuthorizationScopes {

    /** 
     * A controller with no defined authorization scopes 
     */
    @RestApiMountPoint("/default")
    @RequestMapping("/api/v1")
    public static class DefaultController {

        @RequestMapping(value = "/default", method = RequestMethod.GET)
        public void pub() {
        }
    }

    /**
     * A controller with three authorization scopes, two at class level, one at method level.
     */
    @RestApiMountPoint("/twoscopes")
    @RequestMapping("/api/v1")
    @AuthorizationScope( { "two_scope_service:read", "two_scope_service:admin" } )
    public static class TwoScopeController {

        @AuthorizationScope("two_scope_service:write")
        @RequestMapping(value = "/twoscope", method = RequestMethod.POST)
        public void post() {
        }

        @RequestMapping(value = "/twoscope", method = RequestMethod.GET)
        public void get() {
        }
    }
}
