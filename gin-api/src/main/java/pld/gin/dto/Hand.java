package pld.gin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Hand has a list of Cards which it can group into a Melding.
 * If a Card could join more than one meld, there could be more than one Melding,
 * so we keep all possible Meldings sorted by increasing deadwood score.
 * A Hand may be temporarily holding an eleventh card.
 */
public class Hand {

    private List<Card> cards;
    private List<Melding> meldings;

    @JsonCreator
    public Hand (@JsonProperty("cards") List<Card> cards) {
        initialize(cards);
    }

    public List<Card> getCards () {
        return cards;
    }

    public List<Melding> getMeldings () {
        return meldings;
    }

    public void setCards (List<Card> cards) {
        initialize(cards);
    }

    @JsonProperty
    public Set<String> getKnockables () {
        Set<String> rv = new HashSet<>();
        meldings.stream().forEach(m -> rv.addAll(m.getKnockables()));
        return rv;
    }

    public void drawCard (Card card) {
        cards.add(card);
        Collections.sort(cards, Card.FACE_COMP);
        findMeldings();
    }

    public void discard (Card card) {
        cards.remove(card);
        findMeldings();
    }

    public boolean cardWouldHelp (Card card) {
        for (Melding m : meldings) {
            if (m.cardWouldHelp(card)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card.getAbbreviation()).append(" ");
        }
        return sb.toString();
    }

    private void initialize (List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        Collections.sort(this.cards, Card.FACE_COMP);
        findMeldings();
    }

    private void findMeldings () {
        List<Card> cards = new ArrayList<>(this.cards);
        List<Group> melds = new ArrayList<>();
        meldings = new ArrayList<>();
        findMeldingsAux(cards, melds);
        meldings = meldings.stream()
                .sorted((a, b) -> a.getScore() - b.getScore())
                .collect(Collectors.toList());
    }

    private void findMeldingsAux(List<Card> cards, List<Group> melds) {
        // find all possible melds, regardless of overlap
        List<Group> localMelds = new ArrayList<>();

        Collections.sort(cards, Card.SUIT_COMP);
        findSuitMelds(cards, localMelds);

        Collections.sort(cards, Card.FACE_COMP);
        findFaceMelds(cards, localMelds, true);

        // map each card to a list of the melds it belongs to
        Map<Card, List<Group>> meldsByCard = new HashMap<>();
        for (Card c : cards) {
            meldsByCard.put(c, new ArrayList<>());
        }
        for (Group meld : localMelds) {
            for (Card card : meld.getCards()) {
                meldsByCard.get(card).add(meld);
            }
        }

        // bad cards, or badz, are cards that belong to more than one meld
        Set<Card> badz = cards.stream()
                .filter(c -> meldsByCard.get(c).size() > 1)
                .collect(Collectors.toSet());

        // clean melds have no bad cards; dirty melds have at least one
        List<Group> cleanMelds = new ArrayList<>();
        List<Group> dirtyMelds = new ArrayList<>();
        for (Group meld : localMelds) {
            boolean bad = false;
            for (Card card : meld.getCards()) {
                if (badz.contains(card)) {
                    bad = true;
                }
            }
            if (bad) {
                dirtyMelds.add(meld);
            } else {
                cleanMelds.add(meld);
            }
        }

        // add clean melds to our output, and eliminate their cards from further consideration
        for (Group meld : cleanMelds) {
            for (Card card : meld.getCards()) {
                cards.remove(card);
            }
            melds.add(meld);
        }

        if (dirtyMelds.size() == 0) {
            List<Group> almosts = new ArrayList<>();
            Collections.sort(cards, Card.SUIT_COMP);
            findSuitAlmosts(cards, almosts);
            Collections.sort(cards, Card.FACE_COMP);
            findFaceMelds(cards, almosts, false);
            meldings.add(new Melding(melds, almosts, cards));
        } else {
            Card bad = badz.iterator().next();
            for (Group meld : meldsByCard.get(bad)) {
                List<Group> melds2 = new ArrayList<>(melds);
                List<Card> cards2 = new ArrayList<>(cards);
                for (Card card : meld.getCards()) {
                    cards2.remove(card);
                }
                melds2.add(meld);
                findMeldingsAux(cards2, melds2);
            }
        }
    }

    private void findSuitMelds (List<Card> cards, List<Group> melds) {
        // The cards are sorted first by suit, then by face.
        int i = 0;
        while (i < cards.size()) {
            List<Card> crdz = new ArrayList<>();
            crdz.add(cards.get(i));
            Suit suit = cards.get(i).getSuit();
            int ord = cards.get(i).getOrdinal();
            int j = i + 1;
            while (j < cards.size() && cards.get(j).getSuit() == suit && cards.get(j).getOrdinal() == ord + 1) {
                crdz.add(cards.get(j));
                ord += 1;
                j += 1;
            }
            if (crdz.size() >= 3) {
                melds.add(new Group(crdz, false));
            }
            i = j;
        }
    }

    private void findSuitAlmosts (List<Card> cards, List<Group> almosts) {
        int i = 0;
        List<Card> crdz = new ArrayList<>();
        while (i < cards.size() - 1) {
            if (cards.get(i).getSuit() == cards.get(i + 1).getSuit() && cards.get(i + 1).getOrdinal() - cards.get(i).getOrdinal() == 1) {
                crdz.clear();
                crdz.add(cards.get(i));
                crdz.add(cards.get(i + 1));
                almosts.add(new Group(crdz, false));
            }
            if (cards.get(i).getSuit() == cards.get(i + 1).getSuit() && cards.get(i + 1).getOrdinal() - cards.get(i).getOrdinal() == 2) {
                crdz.clear();
                crdz.add(cards.get(i));
                crdz.add(cards.get(i + 1));
                almosts.add(new Group(crdz, false));
            }
            i += 1;
        }
    }

    private void findFaceMelds (List<Card> cards, List<Group> melds, boolean complete) {
        // The cards are sorted by face.
        int i = 0;
        while (i < cards.size()) {
            List<Card> crdz = new ArrayList<>();
            crdz.add(cards.get(i));
            Face face = cards.get(i).getFace();
            int j = i + 1;
            while (j < cards.size() && cards.get(j).getFace() == face) {
                crdz.add(cards.get(j));
                j += 1;
            }
            if (complete && crdz.size() >= 3 || !complete && crdz.size() == 2) {
                melds.add(new Group(crdz, true));
            }
            i = j;
        }
    }
}
