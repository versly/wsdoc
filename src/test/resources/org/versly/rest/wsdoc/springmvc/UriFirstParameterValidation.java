import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RequestMapping("${base-service.server.api-path:}/api/v1/")
public class UriFirstParameterValidation {
    /**
     * Some description of the group.
     * @param id The participant's parent identifier.
     */
    @RequestMapping(value = "/group/{id}/participant", method = RequestMethod.GET)
    public void getParticipant(@PathVariable("id") String id) {
    }
}