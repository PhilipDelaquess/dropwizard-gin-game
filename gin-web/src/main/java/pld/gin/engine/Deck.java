package pld.gin.engine;

import pld.gin.dto.Card;

import java.util.Collections;
import java.util.List;

/**
 * Created by philip on 8/31/16.
 */
public class Deck {

    private List<Card> cards;

    public Deck () {
        initialize();
    }

    public void initialize () {
        cards = Card.getAll();
        Collections.shuffle(cards);
    }

    public int getRemaining () {
        return cards.size();
    }

    public List<Card> dealTen () {
        List<Card> rv = cards.subList(0, 10);
        cards = cards.subList(10, cards.size());
        return rv;
    }

    public Card dealOne () {
        Card rv = cards.get(0);
        cards.remove(0);
        return rv;
    }

}
