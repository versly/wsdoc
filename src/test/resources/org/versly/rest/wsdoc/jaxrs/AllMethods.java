package org.versly.rest.wsdoc.jaxrs;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;

public class AllMethods {

    @GET
    @Path("allMethodsGet")
    public void allMethodsGet() {
    }

    @POST
    @Path("allMethodsPost")
    public void allMethodsPost() {
    }

    @PUT
    @Path("allMethodsPut")
    public void allMethodsPut() {
    }

    @DELETE
    @Path("allMethodsDelete")
    public void allMethodsDelete() {
    }

    @HEAD
    @Path("allMethodsHead")
    public void allMethodsHead() {
    }

    @OPTIONS
    @Path("allMethodsOptions")
    public void allMethodsOptions() {
    }
}
