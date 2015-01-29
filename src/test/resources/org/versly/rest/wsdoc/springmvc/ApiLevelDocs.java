import org.springframework.web.bind.annotation.*;
import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.DocumentationRestApi;

@DocumentationRestApi(id = "UltimateApi", title = "The Ultimate REST API", version = "v1")
@DocumentationScope("public")
@RequestMapping("/ultimate/api/v1")
public class ApiLevelDocs {

    /**
     * Some description of the widgets.
     * @param id The widget identifier.
     */
    @RequestMapping(value = "/widgets", method = RequestMethod.GET)
    public void getWidget() {
    }
}