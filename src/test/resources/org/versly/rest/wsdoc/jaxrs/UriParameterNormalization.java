import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class UriParameterNormalization {

    /**
     * Some description of the widgets.
     * @param id The widget identifier documented in GET.
     */
    @GET
    @Path("/widgets/{id}")
    public void getWidget(@PathParam("id") String id) {
    }

    /**
     * Some description of the widgets.
     * @param id The widget identifier documented in POST.
     */
    @POST
    @Path("/widgets/{id}")
    public void createWidget(@PathParam("id") String id) {
    }

    /**
     * Some description of the gadgets.
     * @param id The gadget's parent identifier.
     */
    @GET
    @Path("/widgets/{id}/gadgets")
    public void getGadgets(@PathVariable("id") String id) {
    }
}