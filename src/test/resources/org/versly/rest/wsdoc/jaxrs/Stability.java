/**
 * This endpoint has private publication scope.
 */

import org.versly.rest.wsdoc.DocumentationDeprecated;
import org.versly.rest.wsdoc.DocumentationExperimental;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class Stability {

    /**
     * A controller with all stable methods. 
     */
    public static class StableController {

        @GET
        @Path("/stable1")
        public void stable() {
        }
    }

    /**
     * A controller with all deprecated (implicit) methods.
     */
    @DocumentationDeprecated
    public static class DeprecatedController {

        @GET
        @Path("/deprecated2")
        public void deprecated() {
        }
    }

    /**
     * A controller with some stable, deprecated, experimental, and deprecated experimental methods.
     */
    public static class MixedController {

        @GET
        @Path("/stable3")
        public void stable() {
        }

        @DocumentationDeprecated
        @GET
        @Path("/deprecated3")
        public void deprecated() {
        }

        @DocumentationExperimental
        @GET
        @Path("/experimental3")
        public void experimental() {
        }

        @DocumentationDeprecated
        @DocumentationExperimental
        @GET
        @Path("/experimentaldeprecated3")
        public void experimentaldeprecated() {
        }
    }
}
