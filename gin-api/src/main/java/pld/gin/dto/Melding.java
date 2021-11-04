package pld.gin.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Melding is one possible way to partition a Hand of Cards into melds, almosts, and deadwood.
 */
public class Melding {

    private static final int MAX_DEADWOOD = 30;

    private final List<Group> melds;
    private final List<Group> almosts;
    private final List<Card> deadwood;
    private final List<Card> orphans;
    private final int score;
    private final Set<String> knockables;
    private final Map<String, Integer> helpers;

    public Melding () {
        melds = null;
        almosts = null;
        deadwood = null;
        orphans = null;
        score = 0;
        knockables = null;
        helpers = null;
    }

    public Melding (List<Group> melds, List<Group> almosts, List<Card> deadwood) {
        this.melds = new ArrayList<>(melds);
        this.almosts = new ArrayList<>(almosts);
        Set<Card> almostParticipants = new HashSet<>();
        for (Group g : almosts) {
            for (Card c : g.getCards()) {
                almostParticipants.add(c);
            }
        }
        this.deadwood = new ArrayList<>(deadwood);
        this.score = this.deadwood.stream().mapToInt(Card::getScore).sum();

        int nCards = melds.stream().mapToInt(m -> m.getCards().size()).sum() + deadwood.size();
        if (nCards == 11) {
            this.knockables = deadwood.stream()
                    .filter(c -> this.score - c.getScore() < MAX_DEADWOOD)
                    .map(Card::getAbbreviation)
                    .collect(Collectors.toSet());
        } else {
            this.knockables = Collections.emptySet();
        }


        helpers = new HashMap<>();
        findHelpers(melds);
        findHelpers(almosts);
        this.orphans = this.deadwood.stream()
                .filter(c -> !almostParticipants.contains(c))
                .collect(Collectors.toList());
    }

    public List<Group> getMelds () {
        return melds;
    }

    public List<Group> getAlmosts () { return almosts; }

    public List<Card> getDeadwood () { return deadwood; }

    public List<Card> getOrphans () {
        return orphans;
    }

    public int getScore () { return score; }

    public Set<String> getKnockables () {
        return knockables;
    }

    public boolean cardWouldHelp (Card card) {
        return helpers.containsKey(card.getAbbreviation());
    }

    private void findHelpers (List<Group> groups) {
        for (Group group : groups) {
            for (Card card : group.getHelpers()) {
                String key = card.getAbbreviation();
                if (helpers.containsKey(key)) {
                    helpers.put(key, 1 + helpers.get(key));
                } else {
                    helpers.put(key, 1);
                }
            }
        }
    }

    public String toString () {
        StringBuilder sb = new StringBuilder();
        sb.append("Score: ").append(score).append("\n");
        if (melds.size() > 0) {
            sb.append("Melds:\n");
            for (Group g : melds) {
                sb.append("   ").append(g.toString()).append("\n");
            }
        }
        if (almosts.size() > 0) {
            sb.append("Almosts:\n");
            for (Group g : almosts) {
                sb.append("   ").append(g.toString()).append("\n");
            }
        }
        sb.append("Deadwood:");
        for (Card c : deadwood) {
            sb.append(" ").append(c.getAbbreviation());
        }
        sb.append("\n");
        sb.append("Orphans:");
        for (Card c : orphans) {
            sb.append(" ").append(c.getAbbreviation());
        }
        sb.append("\n");
        sb.append("Helpers:");
        for (Map.Entry<String, Integer> entry : helpers.entrySet()) {
            sb.append(" ").append(entry.getKey()).append("(").append(entry.getValue()).append(")");
        }
        sb.append("\n");
        return sb.toString();
    }
}
