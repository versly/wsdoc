package org.versly.rest.wsdoc.springmvc.genericdomain;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.versly.rest.wsdoc.model.WildcardTypeReferrer;

public class WildcardController {

    /**
     * Retrieves a field that has a wildcard in its nested fields
     */
    @RequestMapping(value = "/foo", method = RequestMethod.GET)
    public WildcardTypeReferrer getFoo() {
        return null;
    }

}
