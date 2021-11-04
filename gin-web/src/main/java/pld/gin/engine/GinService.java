package pld.gin.engine;

import org.jvnet.hk2.annotations.Service;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pld.gin.dto.Command;
import pld.gin.dto.Player;
import pld.gin.dto.PlayerState;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The GinService creates new Player instances as players arrive, and creates a new Game instance
 * for each pair of players.
 *
 * @author philip
 */
@Service
public class GinService {
    static final Logger LOG = LoggerFactory.getLogger(GinService.class);

    private final Map<String, Player> players = new HashMap<>();
    private final Map<String, Game> games = new HashMap<>();

    private Player pending = null;

    private DBI jdbi;

    public GinService (DBI jdbi) {
        this.jdbi = jdbi;
    }

    public Player createPlayer (String name) {
        String id = String.valueOf(new Date().getTime());;
        Player player = new Player(id, name);
        players.put(id, player);
        if (pending == null) {
            LOG.debug("createPlayer: {}  - waiting for opponent", name);
            pending = player;
            player.setState(PlayerState.AWAITING_OPPONENT_ARRIVAL);
        } else {
            LOG.debug("createPlayer: {}  - paired with {}", name, pending.getName());
            player.setOpponentId(pending.getId());
            player.setOpponentName(pending.getName());
            pending.setOpponentId(player.getId());
            pending.setOpponentName(player.getName());

            Game game = new Game(pending, player);
            games.put(pending.getId(), game);
            games.put(player.getId(), game);

            pending = null;
        }

        return player;
    }

    public Player command (Command action) {
        String id = action.getId();
        Player player = players.get(id);
        Player opponent = players.get(player.getOpponentId());
        Game game = games.get(id);
        game.command(player, opponent, action);
        return player;
    }

    public Player getPlayer (String id) {
        if (id == null) {
            throw new IllegalArgumentException("player uuid may not be null");
        }
        if (players.containsKey(id)) {
            return players.get(id);
        } else {
            return null;
        }
    }

}
