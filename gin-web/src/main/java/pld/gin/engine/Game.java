package pld.gin.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pld.gin.dto.Card;
import pld.gin.dto.Command;
import pld.gin.dto.DealSummary;
import pld.gin.dto.Hand;
import pld.gin.dto.Melding;
import pld.gin.dto.Player;
import pld.gin.dto.PlayerState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A Game begins by randomly picking a dealer, who shuffles and deals.
 * Then a hand is played. The winner deals the next hand.
 * Eventually a player exceeds 100 points and the Game ends.
 */
public class Game {
    private static final Logger LOG = LoggerFactory.getLogger(Game.class);

    private static final int POINTS_TO_WIN = 100;

    private final Player player1;
    private final Player player2;
    private Deck deck;
    private List<Card> discard;

    public Game (Player p1, Player p2) {
        player1 = p1;
        player2 = p2;

        newDeal();

        Player dealer, pone;
        if (new Date().getTime() % 2 == 0) {
            dealer = player1;
            pone = player2;
        } else {
            dealer = player2;
            pone = player1;
        }

        setStates(dealer, PlayerState.AWAITING_OPPONENT_ACTION, pone, PlayerState.PONE_INITIAL_DRAW);
        LOG.debug("new Game with dealer {} and non-dealer {}", dealer.getName(), pone.getName());
    }

    private void newDeal () {
        deck = new Deck();
        player1.setHand(new Hand(deck.dealTen()));
        player2.setHand(new Hand(deck.dealTen()));

        discard = new ArrayList<>();
        Card topDiscard = deck.dealOne();
        discard.add(topDiscard);
        player1.setTopDiscard(topDiscard);
        player2.setTopDiscard(topDiscard);

        int packSize = deck.getRemaining();
        player1.setPackSize(packSize);
        player2.setPackSize(packSize);

    }

    private void setStates(Player p1, PlayerState p1state, Player p2, PlayerState p2state) {
        p1.setState(p1state);
        p1.setOpponentState(p2state);
        p2.setState(p2state);
        p2.setOpponentState(p1state);
    }

    private void drawDiscard (Player player, Player opponent) {
        Card card = discard.get(0);
        discard.remove(0);
        player.getHand().drawCard(card);
        player.setLastDraw(card);
        player.setFromDiscard(true);
        opponent.setOpponentLastDraw(card);

        Card top = discard.size() > 0 ? discard.get(0) : null;
        player.setTopDiscard(top);
        opponent.setTopDiscard(top);
    }

    private void drawPack (Player player, Player opponent) {
        Card card = deck.dealOne();
        player.getHand().drawCard(card);
        player.setLastDraw(card);
        player.setFromDiscard(false);
        opponent.setOpponentLastDraw(null);

        int size = deck.getRemaining();
        player.setPackSize(size);
        opponent.setPackSize(size);
    }

    private void discard (Player player, Player opponent, Card card) {
        player.getHand().discard(card);
        player.setLastDraw(null);
        discard.add(0, card);
        player.setTopDiscard(card);
        opponent.setTopDiscard(card);
    }

    /*
     * You could have a map of player states to maps of commands to handler lambdas.
     * Instead, I'll break it out procedurally, but keep the map if this gets unweildy.
     */

    public void command (Player player, Player opponent, Command command) {
        switch (player.getState()) {
            case PONE_INITIAL_DRAW :
                poneInitialDrawCommand (player, opponent, command);
                break;
            case DEALER_INITIAL_DRAW :
                dealerInitialDrawCommand (player, opponent, command);
                break;
            case DISCARD_OR_KNOCK :
                discardOrKnock (player, opponent, command);
                break;
            case NORMAL_DRAW :
                normalDraw (player, opponent, command);
                break;
            case ACKNOWLEDGE_DEAL :
                acknowledgeDeal(player, opponent, command);
                break;
            default :
                // no commands are legal -- throw?
                break;
        }
    }

    private void poneInitialDrawCommand (Player player, Player opponent, Command command) {
        switch (command.getAction()) {
            case DRAW_DISCARD :
                drawDiscard(player, opponent);
                setStates(player, PlayerState.DISCARD_OR_KNOCK, opponent, PlayerState.AWAITING_OPPONENT_ACTION);
                break;
            case REJECT_INITIAL :
                setStates(player, PlayerState.AWAITING_OPPONENT_ACTION, opponent, PlayerState.DEALER_INITIAL_DRAW);
                break;
        }
    }

    private void dealerInitialDrawCommand (Player player, Player opponent, Command command) {
        switch (command.getAction()) {
            case DRAW_DISCARD :
                drawDiscard(player, opponent);
                setStates(player, PlayerState.DISCARD_OR_KNOCK, opponent, PlayerState.AWAITING_OPPONENT_ACTION);
                break;
            case REJECT_INITIAL :
                drawPack(opponent, player);
                setStates(player, PlayerState.AWAITING_OPPONENT_ACTION, opponent, PlayerState.DISCARD_OR_KNOCK);
                break;
        }
    }

    private void discardOrKnock (Player player, Player opponent, Command command) {
        Card card = Card.byAbbreviation(command.getAbbreviation());
        switch (command.getAction()) {
            case DISCARD :
                discard(player, opponent, card);
                setStates(player, PlayerState.AWAITING_OPPONENT_ACTION, opponent, PlayerState.NORMAL_DRAW);
                break;
            case KNOCK :
                discard(player, opponent, card);
                knock(player, opponent);
                setStates(player, PlayerState.ACKNOWLEDGE_DEAL, opponent, PlayerState.ACKNOWLEDGE_DEAL);
                break;
        }
    }

    private void normalDraw (Player player, Player opponent, Command command) {
        switch (command.getAction()) {
            case DRAW_PACK :
                drawPack(player, opponent);
                setStates(player, PlayerState.DISCARD_OR_KNOCK, opponent, PlayerState.AWAITING_OPPONENT_ACTION);
                break;
            case DRAW_DISCARD :
                drawDiscard(player, opponent);
                setStates(player, PlayerState.DISCARD_OR_KNOCK, opponent, PlayerState.AWAITING_OPPONENT_ACTION);
                break;
        }
    }

    private void acknowledgeDeal (Player player, Player opponent, Command command) {
        switch (command.getAction()) {
            case ACKNOWLEDGE:
                if (player.getScore() >= POINTS_TO_WIN || opponent.getScore() >= POINTS_TO_WIN) {
                    player.setScore(0);
                    player.setOpponentScore(0);
                    opponent.setScore(0);
                    opponent.setOpponentScore(0);
                }
                if (opponent.getState() == PlayerState.ACKNOWLEDGE_DEAL) {
                    // opponent has not OKed the deal
                    setStates(player, PlayerState.AWAITING_OPPONENT_ACTION, opponent, PlayerState.ACKNOWLEDGE_DEAL);
                } else {
                    // opponent has already OKed
                    boolean playerKnocked = player.getSummary().getKnockerId().equals(player.getId());
                    boolean undercut = player.getSummary().isUndercut();
                    if (playerKnocked != undercut) {
                        // player is next dealer
                        setStates(player, PlayerState.AWAITING_OPPONENT_ACTION, opponent, PlayerState.PONE_INITIAL_DRAW);
                    } else {
                        // opponent is next dealer
                        setStates(player, PlayerState.PONE_INITIAL_DRAW, opponent, PlayerState.AWAITING_OPPONENT_ACTION);
                    }
                }
                break;
        }
    }

    private void knock (Player player, Player opponent) {
        DealSummary summary = new DealSummary();
        summary.setKnockerId(player.getId());
        player.setSummary(summary);
        opponent.setSummary(summary);

        Melding playerMelding = player.getHand().getMeldings().get(0);
        summary.setKnockerMelding(playerMelding);
        int pmScore = playerMelding.getScore();
        Melding opponentMelding = opponent.getHand().getMeldings().get(0);
        summary.setOtherMelding(opponentMelding);
        // TODO - lay off opponent's deadwood on player's melds
        int omScore = opponentMelding.getScore();
        int playerScore = 0, opponentScore = 0;
        if (pmScore < omScore) {
            playerScore = omScore - pmScore;
            if (pmScore == 0) {
                playerScore += 25;
                summary.setGin(true);
            }
            summary.setScore(playerScore);
        } else {
            opponentScore = pmScore - omScore + 25;
            summary.setScore(opponentScore);
            summary.setUndercut(true);
        }

        player.setScore(player.getScore() + playerScore);
        player.setOpponentScore(player.getOpponentScore() + opponentScore);
        opponent.setScore(opponent.getScore() + opponentScore);
        opponent.setOpponentScore(opponent.getOpponentScore() + playerScore);

        newDeal();
    }
}
