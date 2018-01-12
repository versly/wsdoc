package org.versly.rest.wsdoc.springmvc.genericdomain;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.WebAsyncTask;

import org.versly.rest.wsdoc.model.genericdomain.Child;

public class AsyncController {

    /**
     * An async controller method. Should be documented just as a child return.
     */
    @RequestMapping(value = "/child", method = RequestMethod.GET)
    public WebAsyncTask<Child> getChild() {
        return null;
    }

}
