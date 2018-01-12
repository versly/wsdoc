package org.versly.rest.wsdoc.springmvc.genericdomain;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.versly.rest.wsdoc.model.WildcardTypeReferrer;

public class WildcardController {

    /**
     * Retrieves the parent
     */
    @RequestMapping(value = "/foo", method = RequestMethod.GET)
    public WildcardTypeReferrer getFoo() {
        return null;
    }

}
