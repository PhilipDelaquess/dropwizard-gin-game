package pld.gin.dto;

/**
 * This describes the post-knock results of a deal.
 */
public class DealSummary {

    private String knockerId;
    private Melding knockerMelding;
    private Melding otherMelding;
    private int score;
    private boolean gin;
    private boolean undercut;

    public String getKnockerId () {
        return knockerId;
    }

    public void setKnockerId (String id) {
        knockerId = id;
    }

    public Melding getKnockerMelding () {
        return knockerMelding;
    }

    public void setKnockerMelding (Melding m) {
        knockerMelding = m;
    }

    public Melding getOtherMelding () {
        return otherMelding;
    }

    public void setOtherMelding (Melding m) {
        otherMelding = m;
    }

    public int getScore () {
        return score;
    }

    public void setScore (int s) {
        score = s;
    }

    public boolean isGin () {
        return gin;
    }

    public void setGin (boolean g) {
        gin = g;
    }

    public boolean isUndercut () {
        return undercut;
    }

    public void setUndercut (boolean u) {
        undercut = u;
    }
}
