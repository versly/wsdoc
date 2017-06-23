package org.versly.rest.wsdoc.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("classPathOnly")
public class ClassPathOnly {

    @GET
    public void classPathOnlyGet() {
    }
}