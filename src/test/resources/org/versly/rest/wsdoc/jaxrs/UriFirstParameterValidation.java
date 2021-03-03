import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("${base-service.server.api-path:}/api/v1/")
public class UriFirstParameterValidation {
    /**
     * Some description of the group.
     * @param id The participant's parent identifier.
     */
    @GET
    @Path("/group/{id}/participant")
    public void getParticipant(@PathVariable("id") String id) {
    }
}