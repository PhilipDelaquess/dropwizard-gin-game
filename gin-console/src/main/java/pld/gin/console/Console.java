package pld.gin.console;

import pld.gin.client.GinClient;
import pld.gin.dto.Card;
import pld.gin.dto.CommandAction;
import pld.gin.dto.Group;
import pld.gin.dto.Melding;
import pld.gin.dto.Player;
import pld.gin.dto.PlayerState;

import java.util.Scanner;

/**
 * This implements a Gin client that uses a command-line stdio interface.
 */
public class Console {

    public static void main (String[] args) {
        GinClient client = new GinClient("http://localhost:8080");
        Scanner scanner = new Scanner(System.in);

        Player player = null;
        String error = null;

        while (true) {
            System.out.println("= = = = = = = = = = = = = = = = = = = =");
            if (error != null) {
                System.out.println("ERROR: " + error);
                break;
            }
            if (player == null) {
                player = login(client, scanner);
            } else if (player.getState() == PlayerState.AWAITING_OPPONENT_ARRIVAL) {
                System.out.println("Welcome, " + player.getName() + ". Waiting for opponent...");
            } else if (player.getState() == PlayerState.ACKNOWLEDGE_DEAL) {
                player = showDealScore(player, client, scanner);
            } else {
                player = showGame(player, client, scanner);
            }
            player = waitForTurn(client, player);
        }
    }

    private static Player login (GinClient client, Scanner scanner) {
        System.out.print("What is your name? ");
        String name = scanner.next();
        return client.newPlayer(name);
    }

    private static Player showGame(Player player, GinClient client, Scanner scanner) {
        for (Melding melding : player.getHand().getMeldings()) {
            showMelding(melding);
        }
        showDeckAndDiscard(player);
        String stateMsg = "";
        String opponentName = player.getOpponentName();
        PlayerState opponentState = player.getOpponentState();
        if (player.getState() == PlayerState.AWAITING_OPPONENT_ACTION) {
            if (opponentState == PlayerState.PONE_INITIAL_DRAW) {
                stateMsg = "You dealt. Waiting for " + opponentName + " to take the initial discard.\n";
            } else if (opponentState == PlayerState.DEALER_INITIAL_DRAW) {
                stateMsg = "Waiting for " + opponentName + " to take the initial discard.\n";
            } else if (opponentState == PlayerState.NORMAL_DRAW) {
                stateMsg = "Waiting for " + opponentName + " to draw.\n";
            } else if (opponentState == PlayerState.DISCARD_OR_KNOCK) {
                if (player.getOpponentLastDraw() != null) {
                    stateMsg = opponentName + " drew from the discard pile ";
                } else {
                    stateMsg = opponentName + " drew from the deck ";
                }
                stateMsg += "and needs to discard.\n";
            } else if (opponentState == PlayerState.ACKNOWLEDGE_DEAL) {
                stateMsg = "Waiting for " + opponentName + " to OK the last deal.\n";
            }
        } else if (player.getState() == PlayerState.PONE_INITIAL_DRAW) {
            stateMsg = opponentName + " dealt. Do you want the initial discard? ";
        } else if (player.getState() == PlayerState.DEALER_INITIAL_DRAW) {
            stateMsg = opponentName + " declined. Do you want the initial discard? ";
        } else if (player.getState() == PlayerState.NORMAL_DRAW) {
            stateMsg = "Your turn. Do you want the top discard? ";
        } else if (player.getState() == PlayerState.DISCARD_OR_KNOCK) {
            stateMsg = "What do you want to discard? ";
        }
        System.out.print(stateMsg);
        if (player.getState() == PlayerState.PONE_INITIAL_DRAW || player.getState() == PlayerState.DEALER_INITIAL_DRAW) {
            if (saidYes(scanner)) {
                player = client.command(player, CommandAction.DRAW_DISCARD, null);
            } else {
                player = client.command(player, CommandAction.REJECT_INITIAL, null);
            }
        } else if (player.getState() == PlayerState.NORMAL_DRAW) {
            if (saidYes(scanner)) {
                player = client.command(player, CommandAction.DRAW_DISCARD, null);
            } else {
                player = client.command(player, CommandAction.DRAW_PACK, null);
                System.out.println("You drew the " + player.getLastDraw().getAbbreviation());
            }
        } else if (player.getState() == PlayerState.DISCARD_OR_KNOCK) {
            String answer = scanner.next();
            if (answer.endsWith("!")) {
                answer = answer.substring(0, answer.length() - 1);
                player = client.command(player, CommandAction.KNOCK, answer);
            } else {
                player = client.command(player, CommandAction.DISCARD, answer);
            }
        }
        return player;
    }

    private static Player showDealScore (Player player, GinClient client, Scanner scanner) {
        System.out.println(player.getName() + ": " + player.getScore() + ", "
                + player.getOpponentName() + ": " + player.getOpponentScore());

        boolean playerKnocked = player.getSummary().getKnockerId().equals(player.getId());
        String knockName = playerKnocked ? "You" : player.getOpponentName();
        System.out.println(knockName + " knocked with");
        showMelding(player.getSummary().getKnockerMelding());

        String otherName = playerKnocked ? player.getOpponentName() : "You";
        System.out.println(otherName + " had");
        showMelding(player.getSummary().getOtherMelding());

        String scoreName = playerKnocked != player.getSummary().isUndercut() ? "You" : player.getOpponentName();
        String stmt = scoreName + " gained " + player.getSummary().getScore() + " points";
        if (player.getSummary().isGin())  {
            stmt += " with GIN!";
        } else if (player.getSummary().isUndercut()) {
            stmt += " by UNDERCUT!";
        }
        System.out.println(stmt);

        String gameOverMsg = player.getScore() >= 100 | player.getOpponentScore() >= 100
                ? scoreName + " WON!"
                : null;
        if (gameOverMsg != null) {
            System.out.println(gameOverMsg);
        }

        System.out.print("OK? ");
        scanner.next();
        return client.command(player, CommandAction.ACKNOWLEDGE, null);
    }

    static void showMelding (Melding melding) {
        for (Group meld : melding.getMelds()) {
            System.out.print(meld.toString());
            System.out.print(" ");
        }
        for (Card card : melding.getDeadwood()) {
            System.out.print(card.getAbbreviation());
            System.out.print(" ");
        }
        System.out.println("(" + melding.getScore() + ")");
    }

    static void showDeckAndDiscard (Player player) {
        System.out.println("Deck: " + player.getPackSize() + " Discard: " +
                (null == player.getTopDiscard()
                ? "(none)"
                : player.getTopDiscard().getAbbreviation()));
    }

    static boolean saidYes (Scanner scanner) {
        String answer = scanner.next();
        return "y".equals(answer) || "Y".equals(answer);
    }

    private static Player waitForTurn (GinClient client, Player player) {
        if (player.getState() == PlayerState.AWAITING_OPPONENT_ARRIVAL) {
            System.out.println("Waiting for opponent to arrive...");
            while (player.getState() == PlayerState.AWAITING_OPPONENT_ARRIVAL) {
                player = waitAndPoll(client);
            }
            System.out.println(player.getOpponentName() + " has arrived to play with you.");
        } else if (player.getState() == PlayerState.AWAITING_OPPONENT_ACTION) {
            if (player.getOpponentState() == PlayerState.ACKNOWLEDGE_DEAL) {
                System.out.println("\nWaiting for " + player.getOpponentName() + " to ack the deal");
            } else {
                System.out.println("\nWaiting for " + player.getOpponentName() + " to move");
            }
            PlayerState lastOppState = player.getOpponentState();
            while (player.getState() == PlayerState.AWAITING_OPPONENT_ACTION) {
                player = waitAndPoll(client);
                while (player.getState() == PlayerState.AWAITING_OPPONENT_ACTION) {
                    player = waitAndPoll(client);
                    PlayerState newOppState = player.getOpponentState();
                    if (newOppState == PlayerState.DISCARD_OR_KNOCK && newOppState != lastOppState) {
                        Card draw = player.getOpponentLastDraw();
                        if (player.getOpponentLastDraw() != null) {
                            System.out.println(player.getOpponentName() + " drew the " + draw.getAbbreviation()
                                    + " from the discard pile.");
                        } else {
                            System.out.println(player.getOpponentName() + " drew from the pack.");
                        }
                        lastOppState = newOppState;
                    }
                }
/*
                System.out.println(player.getOpponentName() + " discarded the "
                        + player.getTopDiscard().getAbbreviation());
*/
            }
        }
        return player;
    }

    private static Player waitAndPoll (GinClient client) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {}
        return client.getCurrentState();
    }
}
