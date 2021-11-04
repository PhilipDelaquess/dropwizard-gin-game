package pld.gin.rest;

import static pld.gin.GinServerApplication.GIN_SERVICE;

import pld.gin.dto.Command;
import pld.gin.dto.Player;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Main REST resource for the Gin Service.
 */

@Path("/gin-server")
@Produces(MediaType.APPLICATION_JSON)
public class ServerResource {


    @POST
    @Path("/new-player")
    @Consumes("text/plain")
    public Player createPlayer (String name) {
        return GIN_SERVICE.createPlayer(name);
    }

    @POST
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    public Player command (Command action) {
        return GIN_SERVICE.command(action);
    }

    @GET
    @Path("/{id}")
    public Player getPlayer (@PathParam("id") String id) {
        return GIN_SERVICE.getPlayer(id);
    }
}
