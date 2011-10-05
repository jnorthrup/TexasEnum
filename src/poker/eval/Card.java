/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package poker.eval;

/**
 * @deprecated
 */
public final class Card implements Comparable<Card> {

    final Face face;
    final Suit suit;

    public Card(Face face, Suit suit) {
        this.suit = suit;
        this.face = face;
    }


    public String toString() {
        return String.valueOf(new char[]{face.desc, suit.desc});
    }

    /**
     * @deprecated
     */
    public int compareTo(Card o) {
        final int faceRank = face.ordinal() - o.face.ordinal();
        if (faceRank != 0) return faceRank;

        return suit.ordinal() - o.suit.ordinal();
    }
}
