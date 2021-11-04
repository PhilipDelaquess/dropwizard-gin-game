package pld.gin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by philip on 8/31/16.
 */
public class Card {

    private final Face face;
    private final Suit suit;
    private final String abbreviation;
    private final int ordinal;
    private final int score;

    private static final ImmutableMap<Face, ImmutableMap<Suit, Card>> faceSuitMap;
    private static final ImmutableMap<String, Card> abbrevMap;

    static {
        ImmutableMap.Builder fsmBuilder = ImmutableMap.builder();
        ImmutableMap.Builder amBuilder = ImmutableMap.builder();
        for (Face f : Face.values()) {
            ImmutableMap.Builder smBuilder = ImmutableMap.builder();
            for (Suit s : Suit.values()) {
                Card c = new Card(f, s);
                smBuilder.put(s, c);
                amBuilder.put(c.abbreviation, c);
            }
            fsmBuilder.put(f, smBuilder.build());
        }
        faceSuitMap = fsmBuilder.build();
        abbrevMap = amBuilder.build();
    }

    private Card (Face f, Suit s) {
        face = f;
        suit = s;
        abbreviation = f.getAbbreviation() + s.name().substring(0, 1);
        ordinal = f.ordinal() + 1;
        score = Math.min(ordinal, 10);
    }

    public static List<Card> getAll () {
        return new ArrayList<>(abbrevMap.values());
    }

    public static Card byFaceAndSuit (Face face, Suit suit) {
        return faceSuitMap.get(face).get(suit);
    }

    @JsonCreator
    public static Card byAbbreviation (@JsonProperty("abbreviation") String abbrev) {
        return abbrevMap.get(abbrev);
    }

    public Face getFace () {
        return face;
    }

    public Suit getSuit () {
        return suit;
    }

    public String getAbbreviation () {
        return abbreviation;
    }

    @JsonIgnore
    public int getOrdinal () {
        return ordinal;
    }

    @JsonIgnore
    public int getScore () {
        return score;
    }

    @Override
    public String toString () {
        return abbreviation;
    }

    public static Comparator<Card> FACE_COMP = new Comparator<Card>() {
        @Override
        public int compare (Card c1, Card c2) {
            return c1.ordinal - c2.ordinal;
        }
    };

    public static Comparator<Card> SUIT_COMP = new Comparator<Card>() {
        @Override
        public int compare (Card c1, Card c2) {
            if (c1.suit != c2.suit) {
                return c1.suit.ordinal() - c2.suit.ordinal();
            } else {
                return c1.ordinal - c2.ordinal;
            }
        }
    };
}
