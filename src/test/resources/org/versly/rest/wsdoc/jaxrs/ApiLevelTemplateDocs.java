package org.versly.rest.wsdoc.jaxrs;

import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.DocumentationRestApi;

import javax.ws.rs.Path;
import javax.ws.rs.GET;

/**
 * Some documentation of the API itself.
 */
@DocumentationRestApi(
        id = DocumentationRestApi.ID_TEMPLATE,
        title = "The Ultimate REST API",
        version = "v1",
        mount = DocumentationRestApi.MOUNT_TEMPLATE)
@DocumentationScope("public")
public class ApiLevelTemplateDocs {

    /**
     * Some description of the widgets.
     * @param id The widget identifier.
     */
    @GET
    @Path("/widgets")
    public void getWidget() {
    }
}