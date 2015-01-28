/**
 * This endpoint has private publication scope.
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.RestApiMountPoint;

public class PublicationScopes {

    /**
     * A controller with default publication scopes 
     */
    @RestApiMountPoint("/default")
    @Path("/api/v1")
    public static class DefaultController {

        @GET
        @Path("/public1")
        public void pub() {
        }
    }

    /**
     * A controller with some public and some private scopes 
     */
    @RestApiMountPoint("/mixed")
    @Path("/api/v1")
    public static class MixedController {

        @DocumentationScope(DocumentationScope.PUBLIC)
        @GET
        @Path("/public2")
        public void pub() {
        }

        @DocumentationScope(DocumentationScope.PRIVATE)
        @GET
        @Path("/private2")
        public void priv() {
        }
    }

    /**
     * A controller with all private scopes. 
     */
    @RestApiMountPoint("/private")
    @Path("/api/v1")
    public static class PrivateController {

        @DocumentationScope(DocumentationScope.PRIVATE)
        @GET
        @Path("/private3")
        public void priv() {
        }
    }

    /**
     * A controller that is private at the class level and containing one public method.
     */
    @DocumentationScope(DocumentationScope.PRIVATE)
    @RestApiMountPoint("/classpriv")
    @Path("/api/v1")
    public static class ClassPrivateController {

        @GET
        @Path("/private4")
        public void priv() {
        }

        @DocumentationScope(DocumentationScope.PUBLIC)
        @GET
        @Path("/public4")
        public void pub() {
        }
    }

    /**
     * A controller that is private at the class level and containing one public method.
     */
    @DocumentationScope(DocumentationScope.PUBLIC)
    @RestApiMountPoint("/classpriv")
    @Path("/api/v1")
    public static class ClassPublicController {

        @GET
        @Path("/public5/foo")
        public void pub1() {
        }

        @DocumentationScope(DocumentationScope.PRIVATE)
        @GET
        @Path("/public5/bar")
        public void pub2() {
        }
    }
}

