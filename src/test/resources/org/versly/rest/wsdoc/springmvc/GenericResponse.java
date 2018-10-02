package org.versly.rest.wsdoc.springmvc;

import java.util.UUID;

import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.versly.rest.wsdoc.ReturnType;
import org.versly.rest.wsdoc.model.genericdomain.DefaultParent;

public class GenericResponse {

    @RequestMapping(value = "/genericResponseGet", method = RequestMethod.GET)
    @ReturnType(UUID.class)
    public Response genericResponseGet() {
        return Response.ok(UUID.randomUUID()).build();
    }

    @RequestMapping(value = "/genericResponsePost", method = RequestMethod.POST)
    @ReturnType(String.class)
    public String genericResponsePost() {
        return UUID.randomUUID().toString();
    }

    @RequestMapping(value = "/genericResponsePut", method = RequestMethod.PUT)
    @ReturnType(UUID.class)
    public UUID genericResponsePut() {
        return UUID.randomUUID();
    }

    @RequestMapping(value = "/genericResponseDelete", method = RequestMethod.DELETE)
    @ReturnType(Response.class)
    public Response genericResponseDelete() {
        return Response.noContent().build();
    }

    @RequestMapping(value = "/genericResponseHead", method = RequestMethod.HEAD)
    @ReturnType(DefaultParent.class)
    public Response genericResponseHead() {
        return Response.noContent().build();
    }

}
