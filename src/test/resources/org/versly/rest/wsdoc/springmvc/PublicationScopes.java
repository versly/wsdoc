/**
 * This endpoint has private publication scope.
 */

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.RestApiMountPoint;

public class PublicationScopes {

    /** 
     * A controller with default publication scopes 
     */
    @RestApiMountPoint("/default")
    @RequestMapping("/api/v1")
    public static class DefaultController {

        @RequestMapping(value = "/public1", method = RequestMethod.GET)
        public void pub() {
        }
    }

    /**
     * A controller with some public and some private scopes 
     */
    @RestApiMountPoint("/mixed")
    @RequestMapping("/api/v1")
    public static class MixedController {

        @DocumentationScope(DocumentationScope.PUBLIC)
        @RequestMapping(value = "/public2", method = RequestMethod.GET)
        public void pub() {
        }

        @DocumentationScope(DocumentationScope.PRIVATE)
        @RequestMapping(value = "/private2", method = RequestMethod.GET)
        public void priv() {
        }
    }

    /**
     * A controller with all private scopes. 
     */
    @RestApiMountPoint("/private")
    @RequestMapping("/api/v1")
    public static class PrivateController {

        @DocumentationScope(DocumentationScope.PRIVATE)
        @RequestMapping(value = "/private3", method = RequestMethod.GET)
        public void priv() {
        }
    }

    /**
     * A controller that is private at the class level and containing one public method.
     */
    @DocumentationScope(DocumentationScope.PRIVATE)
    @RestApiMountPoint("/classpriv")
    @RequestMapping("/api/v1")
    public static class ClassPrivateController {

        @RequestMapping(value = "/private4", method = RequestMethod.GET)
        public void priv() {
        }

        @DocumentationScope(DocumentationScope.PUBLIC)
        @RequestMapping(value = "/pubpriv4", method = RequestMethod.GET)
        public void pub() {
        }
    }

    /**
     * A controller that is private at the class level and containing one public method.
     */
    @DocumentationScope(DocumentationScope.PUBLIC)
    @RestApiMountPoint("/classpriv")
    @RequestMapping("/api/v1")
    public static class ClassPublicController {

        @RequestMapping(value = "/public5/foo", method = RequestMethod.GET)
        public void pub1() {
        }

        @DocumentationScope(DocumentationScope.PRIVATE)
        @RequestMapping(value = "/pubpriv5/bar", method = RequestMethod.GET)
        public void pub2() {
        }
    }

    /**
     * A controller that is "experimental" scope only.
     */
    @DocumentationScope("experimental")
    @RestApiMountPoint("/newshakystuff")
    public static class ExperimentalController {

        @RequestMapping(value = "/foo", method = RequestMethod.GET)
        public void foo() {
        }

        @RequestMapping(value = "/bar", method = RequestMethod.GET)
        public void bar() {
        }
    }
}
