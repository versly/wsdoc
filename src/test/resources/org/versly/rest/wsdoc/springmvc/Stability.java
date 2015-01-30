/**
 * This endpoint has private publication scope.
 */

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.versly.rest.wsdoc.DocumentationDeprecated;
import org.versly.rest.wsdoc.DocumentationExperimental;

public class Stability {

    /**
     * A controller with all stable methods. 
     */
    public static class StableController {

        @RequestMapping(value = "/stable1", method = RequestMethod.GET)
        public void stable() {
        }
    }

    /**
     * A controller with all deprecated (implicit) methods.
     */
    @DocumentationDeprecated
    public static class DeprecatedController {

        @RequestMapping(value = "/deprecated2", method = RequestMethod.GET)
        public void deprecated() {
        }
    }

    /**
     * A controller with some stable, deprecated, experimental, and deprecated experimental methods.
     */
    public static class MixedController {

        @RequestMapping(value = "/stable3", method = RequestMethod.GET)
        public void stable() {
        }

        @DocumentationDeprecated
        @RequestMapping(value = "/deprecated3", method = RequestMethod.GET)
        public void deprecated() {
        }

        @DocumentationExperimental
        @RequestMapping(value = "/experimental3", method = RequestMethod.GET)
        public void experimental() {
        }

        @DocumentationDeprecated
        @DocumentationExperimental
        @RequestMapping(value = "/experimentaldeprecated3", method = RequestMethod.GET)
        public void experimentaldeprecated() {
        }
    }
}
