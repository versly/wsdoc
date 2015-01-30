import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.DocumentationRestApi;

import javax.ws.rs.Path;
import javax.ws.rs.GET;

/**
 * Some documentation of the API itself.
 */
@DocumentationRestApi(id = "UltimateApi", title = "The Ultimate REST API", version = "v1")
@DocumentationScope("public")
@Path("/ultimate/api/v1")
public class ApiLevelDocs {

    /**
     * Some description of the widgets.
     * @param id The widget identifier.
     */
    @GET
    @Path("/widgets")
    public void getWidget() {
    }
}