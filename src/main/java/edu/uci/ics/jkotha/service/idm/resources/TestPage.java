package edu.uci.ics.jkotha.service.idm.resources;

import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("test")
public class TestPage {
    @Path("hello")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(@Context HttpHeaders headers) {
        String receivedMessage = headers.getHeaderString("message");
        String sender = headers.getHeaderString("sender");
        ServiceLogger.LOGGER.info(receivedMessage);
        String responseMessage = "IDM: Hello, " + sender + "!";
        return Response.status(Response.Status.OK).entity(responseMessage).build();
    }
}