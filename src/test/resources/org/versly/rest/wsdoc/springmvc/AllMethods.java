package org.versly.rest.wsdoc.springmvc;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class AllMethods {

    @RequestMapping(value = "/allMethodsGet", method = RequestMethod.GET)
    public void allMethodsGet() {
    }

    @RequestMapping(value = "/allMethodsPost", method = RequestMethod.POST)
    public void allMethodsPost() {
    }

    @RequestMapping(value = "/allMethodsPut", method = RequestMethod.PUT)
    public void allMethodsPut() {
    }

    @RequestMapping(value = "/allMethodsDelete", method = RequestMethod.DELETE)
    public void allMethodsDelete() {
    }

    @RequestMapping(value = "/allMethodsHead", method = RequestMethod.HEAD)
    public void allMethodsHead() {
    }

    @RequestMapping(value = "/allMethodsOptions", method = RequestMethod.OPTIONS)
    public void allMethodsOptions() {
    }

    @RequestMapping(value = "/allMethodsPatch", method = RequestMethod.PATCH)
    public void allMethodsPatch() {
    }
}
