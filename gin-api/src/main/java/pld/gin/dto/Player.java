package pld.gin.dto;

/**
 * Manages the state of a player from initial connection to later removal.
 */
public class Player {
    private final String id;
    private final String name;
    private PlayerState state;
    private int score;
    private Card lastDraw;
    private boolean fromDiscard;

    private String opponentId;
    private String opponentName;
    private PlayerState opponentState;
    private int opponentScore;
    private Card opponentLastDraw;

    private Hand hand;
    private Card topDiscard;
    private int packSize;

    private DealSummary summary;

    public Player () {
        id = null;
        name = null;
    }

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.opponentName = null;
    }

    public String getId () {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlayerState getState () {
        return state;
    }

    public void setState (PlayerState ps) {
        state = ps;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Card getLastDraw () {
        return lastDraw;
    }

    public void setLastDraw (Card card) {
        lastDraw = card;
    }

    public boolean isFromDiscard () {
        return fromDiscard;
    }

    public void setFromDiscard (boolean d) {
        fromDiscard = d;
    }

    public String getOpponentId () {
        return opponentId;
    }

    public void setOpponentId (String id) {
        opponentId = id;
    }

    public String getOpponentName () {
        return opponentName;
    }

    public void setOpponentName (String name) {
        opponentName = name;
    }

    public PlayerState getOpponentState () {
        return opponentState;
    }

    public void setOpponentState (PlayerState ps) {
        opponentState = ps;
    }

    public int getOpponentScore () {
        return opponentScore;
    }

    public void setOpponentScore (int score) {
        opponentScore = score;
    }

    public Card getOpponentLastDraw () {
        return opponentLastDraw;
    }

    public void setOpponentLastDraw (Card card) {
        opponentLastDraw = card;
    }

    public Hand getHand () {
        return hand;
    }

    public void setHand (Hand h) {
        hand = h;
    }

    public Card getTopDiscard () {
        return topDiscard;
    }

    public void setTopDiscard (Card card) {
        topDiscard = card;
    }

    public int getPackSize () {
        return packSize;
    }

    public void setPackSize (int size) {
        packSize = size;
    }

    public DealSummary getSummary () {
        return summary;
    }

    public void setSummary (DealSummary ds) {
        summary = ds;
    }

}
