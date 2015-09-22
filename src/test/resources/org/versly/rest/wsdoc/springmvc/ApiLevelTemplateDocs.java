package org.versly.rest.wsdoc.springmvc;

import org.springframework.web.bind.annotation.*;
import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.DocumentationRestApi;

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
    @RequestMapping(value = "/widgets", method = RequestMethod.GET)
    public void getWidget() {
    }
}