import org.springframework.web.bind.annotation.*;
import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.DocumentationRestApi;

/**
 * Some documentation of the API itself.
 */
@DocumentationRestApi(id = "UltimateApi", title = "The Ultimate REST API", version = "v1", mount = "/ultimate/api/v1")
@DocumentationScope("public")
public class ApiLevelDocs {

    /**
     * Some description of the widgets.
     * @param id The widget identifier.
     */
    @RequestMapping(value = "/widgets", method = RequestMethod.GET)
    public void getWidget() {
    }
}