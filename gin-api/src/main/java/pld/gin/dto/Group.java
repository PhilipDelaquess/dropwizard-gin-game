package pld.gin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Group is a list of two or more Cards that share a face or are adjacent in one suit.
 * If three or more, they form a meld. Otherwise, they form an almost.
 */
public class Group {
    private final List<Card> cards;
    private final boolean isFaceGroup;

    public Group () {
        cards = null;
        isFaceGroup = false;
    }

    public Group (List<Card> cards, boolean isFace) {
        this.cards = new ArrayList<>(cards);
        isFaceGroup = isFace;
    }

    public List<Card> getCards () {
        return cards;
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String sep = "";
        for (Card c : cards) {
            sb.append(sep).append(c.getAbbreviation());
            sep = " ";
        }
        sb.append("]");
        return sb.toString();
    }

    @JsonIgnore
    public Set<Card> getHelpers () {
        if (isFaceGroup) {
            return getSameFaceHelpers();
        } else {
            return getSameSuitHelpers();
        }
    }

    @JsonIgnore
    public String getKey () {
        return cards.stream().map(c -> c.getAbbreviation()).collect(Collectors.joining(","));
    }

    private Set<Card> getSameFaceHelpers() {
        Face face = cards.get(0).getFace();
        Set<Suit> suits = cards.stream()
                .map(Card::getSuit)
                .collect(Collectors.toSet());
        return Arrays.stream(Suit.values())
                .filter(s -> !suits.contains(s))
                .map(s -> Card.byFaceAndSuit(face, s))
                .collect(Collectors.toSet());
    }

    private Set<Card> getSameSuitHelpers () {
        Set<Card> rv = new HashSet<>();
        Suit suit = cards.get(0).getSuit();
        int lowOrdinal = cards.get(0).getOrdinal();
        if (cards.get(1).getOrdinal() - cards.get(0).getOrdinal() == 1) {
            // the usual case -- two or more adjacent cards
            int highOrdinal = cards.get(cards.size() - 1).getOrdinal();
            if (lowOrdinal > 1) {
                rv.add(Card.byFaceAndSuit(Face.values()[lowOrdinal - 2], suit));
            }
            if (highOrdinal < 13) {
                rv.add(Card.byFaceAndSuit(Face.values()[highOrdinal], suit));
            }
        } else {
            // filling an inside straight
            rv.add(Card.byFaceAndSuit(Face.values()[lowOrdinal], suit));
        }
        return rv;
    }
}
