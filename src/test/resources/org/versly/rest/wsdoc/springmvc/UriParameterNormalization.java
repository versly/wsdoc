import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

public class UriParameterNormalization {

        /**
         * Some description of the widgets.
         * @param id The widget identifier.
         */
        @RequestMapping(value = "/widgets/{id}", method = RequestMethod.GET)
        public void getWidget(@PathVariable("id") String id) {
        }

        /**
         * Some description of the gadgets.
         * @param id The gadget's parent identifier.
         */
        @RequestMapping(value = "/widgets/{id}/gadgets", method = RequestMethod.GET)
        public void getGadgets(@PathVariable("id") String id) {
        }
}