package org.versly.rest.wsdoc.jaxrs;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import org.versly.rest.wsdoc.AuthorizationScope;
import org.versly.rest.wsdoc.RestApiMountPoint;

public class AuthorizationScopes {

    /** 
     * A controller with no defined authorization scopes 
     */
    @RestApiMountPoint("/default")
    @Path("/api/v1")
    public static class DefaultController {

        @GET
        @Path("/default")
        public void pub() {
        }
    }

    /**
     * A controller with three authorization scopes, two at class level, one at method level.
     */
    @RestApiMountPoint("/twoscopes")
    @Path("/api/v1")
    @AuthorizationScope( { "two_scope_service:read", "two_scope_service:admin" } )
    public static class TwoScopeController {

        @AuthorizationScope("two_scope_service:write")
        @POST
        @Path("/twoscope")
        public void get() {
        }

        @GET
        @Path("/twoscope")
        public void post() {
        }
    }
}
