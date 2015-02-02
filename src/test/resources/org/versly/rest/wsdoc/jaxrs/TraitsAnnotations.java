package org.versly.rest.wsdoc.jaxrs;

import org.versly.rest.wsdoc.DocumentationTraits;

import javax.swing.text.Document;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class TraitsAnnotations {

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
    @DocumentationTraits(DocumentationTraits.DEPRECATED)
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

        @DocumentationTraits(DocumentationTraits.DEPRECATED)
        @GET
        @Path("/deprecated3")
        public void deprecated() {
        }

        @DocumentationTraits(DocumentationTraits.EXPERIMENTAL)
        @GET
        @Path("/experimental3")
        public void experimental() {
        }

        @DocumentationTraits({ DocumentationTraits.EXPERIMENTAL, DocumentationTraits.DEPRECATED })
        @GET
        @Path("/experimentaldeprecated3")
        public void experimentaldeprecated() {
        }
    }
}
