/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package poker.eval;

import poker.player.*;

import java.util.*;
import java.util.logging.*;

/**
 * User: sam
 * Date: Apr 2, 2005
 * Time: 4:00:43 PM
 */
public final class Deck {

    public static final int FACES_LEN = (int) Face.values().length;
    public static final int SUITS_LEN = (int) Suit.values().length;
    public static final int DECK_SIZE = FACES_LEN * SUITS_LEN;
    static final char[] faces = new char[Face.values().length];
    static final char[] suits = new char[Suit.values().length];
    static private final int[] deck = new int[DECK_SIZE];
    private int[] cards = new int[DECK_SIZE];

    static {
        initFaces();
        initSuits();

        int curs = 0;
        for (int f = 0; f < FACES_LEN; f++)
            for (int s = 0; s < SUITS_LEN; s++) {
                final int v = f << 2 | s & 0x3;
                deck[curs++] = v;
            }

        if (Seat.test)
            Logger.getAnonymousLogger().info(String.valueOf(CardUtil.toChar(deck)));
    }

    private static void initSuits() {
        for (int i = 0; i < suits.length; i++) {
            suits[i] = Suit.values()[i].desc;
        }
        if (Seat.test) Logger.getAnonymousLogger().info(new String(suits));
    }


    private static void initFaces() {
        for (int i = 0; i < faces.length; i++) {
            faces[i] = Face.values()[i].desc;
        }


        if (Seat.test) Logger.getAnonymousLogger().info(new String(faces));


    }


    /**
     * shuffles cards.
     */
    public void shuffle() {

        if (Seat.test) return;

        List<Integer> b = new ArrayList<Integer>();
        for (int aInteger : deck)
            b.add(aInteger);

        Collections.shuffle(b);

        for (int i = 0; i < b.size(); i++)
            cards[i] = b.get(i);

        if (Seat.test) {
            dump();
            Logger.getAnonymousLogger().info(String.valueOf(CardUtil.toChar(cards)));
        }
    }

    public void clear() {
        // Reset position to start
        dealIndex = 0;
    }
    
    private int dealIndex = 0;

    public int deal() {
        return cards[dealIndex++];
    }

    public void burn() {
        deal();
    }

    public int[] getCards() {
        return cards.clone();
    }

    public void setCards(int[] cards) {
        this.cards = cards;
        this.dealIndex = 0;
    }

    public String dump() {
        final String[] strings = new String[Deck.DECK_SIZE];
        for (int i = 0; i < strings.length; i++)
            strings[i] = Integer.toHexString(deck[i]);


        final String s = Arrays.toString(strings);
        Logger.getAnonymousLogger().info(s);
        return s;
    }
}
