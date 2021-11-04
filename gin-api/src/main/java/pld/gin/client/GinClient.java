package pld.gin.client;

import pld.gin.dto.Command;
import pld.gin.dto.CommandAction;
import pld.gin.dto.Player;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * This class has methods for sending GET and POST requests to the server and returning
 * the responses as deserialized entities.
 */
public class GinClient {

    private final Client client;
    private final WebTarget ginServer;
    private String id;

    public GinClient (String url) {
        client = ClientBuilder.newClient();
        ginServer = client.target(url).path("api/gin-server");
    }

    public Player newPlayer (String name) {
        Player rv = ginServer.path("new-player")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(name, MediaType.TEXT_PLAIN), Player.class);
        id = rv.getId();
        return rv;
    }

    public Player getCurrentState () {
        return ginServer.path(id)
                .request(MediaType.APPLICATION_JSON)
                .get(Player.class);
    }

    public Player command (Player player, CommandAction action, String abbrev) {
        //Card card = Card.byAbbreviation(abbrev);
        Command command = new Command(player.getId(), action, abbrev);
        return ginServer.path("action")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(command, MediaType.APPLICATION_JSON), Player.class);
    }
}
