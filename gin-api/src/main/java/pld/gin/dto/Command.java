package pld.gin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by philip on 9/8/16.
 */
public class Command {
    private final String id;
    private final CommandAction action;
    private final String abbreviation;

    @JsonCreator
    public Command (@JsonProperty("id") String id,
                    @JsonProperty("action") CommandAction action,
                    @JsonProperty("abbrev") String abbrev)
    {
        this.id = id;
        this.action = action;
        this.abbreviation = abbrev;
    }

    public String getId () {
        return id;
    }

    public CommandAction getAction () {
        return action;
    }

    public String getAbbreviation () {
        return abbreviation;
    }
}
