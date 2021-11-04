package pld.gin.dto;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by philip on 10/11/16.
 */
public enum Face {
    Ace("A"),
    Two("2"),
    Three("3"),
    Four("4"),
    Five("5"),
    Six("6"),
    Seven("7"),
    Eight("8"),
    Nine("9"),
    Ten("10"),
    Jack("J"),
    Queen("Q"),
    King("K");

    private final String abbreviation;

    Face(String abbrev) {
        abbreviation = abbrev;
    }

    @JsonValue
    public String getAbbreviation () {
        return abbreviation;
    }
}
