package org.versly.rest.wsdoc.jaxrs;

import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.versly.rest.wsdoc.ReturnType;
import org.versly.rest.wsdoc.model.genericdomain.DefaultParent;

public class GenericResponse {

    @GET
    @Path("genericResponseGet")
    @ReturnType(UUID.class)
    public Response genericResponseGet() {
        return Response.ok(UUID.randomUUID()).build();
    }

    @POST
    @Path("genericResponsePost")
    @ReturnType(String.class)
    public String genericResponsePost() {
        return UUID.randomUUID().toString();
    }

    @PUT
    @Path("genericResponsePut")
    @ReturnType(UUID.class)
    public UUID genericResponsePut() {
        return UUID.randomUUID();
    }

    @DELETE
    @Path("genericResponseDelete")
    @ReturnType(Response.class)
    public Response genericResponseDelete() {
        return Response.noContent().build();
    }
    
    @HEAD
    @Path("genericResponseHead")
    @ReturnType(DefaultParent.class)
    public Response genericResponseHead() {
        return null;
    }
}
