/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package poker.eval;

import poker.player.*;

import java.nio.*;
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
    static private final IntBuffer deck = (IntBuffer) IntBuffer.wrap(new int[DECK_SIZE]).mark();
    private IntBuffer cards = (IntBuffer) ((IntBuffer) BuffUtil.allocate(DECK_SIZE)).put(deck).mark();

    static {
        initFaces();
        initSuits();


        final int[] seed = deck.array();

        int curs = 0;
        for (int f = 0; f < FACES_LEN; f++)
            for (int s = 0; s < SUITS_LEN; s++) {
                final int v = f << 16 | s & 0x3;
                seed[curs++] = v;
            }

        if (Seat.test)
            Logger.getAnonymousLogger().info(String.valueOf(CardUtil.toChar((IntBuffer) deck.reset())));
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

        final int[] ints = deck.array();


        List<Integer> b = new ArrayList<Integer>();
        for (int aInteger : ints)
            b.add(aInteger);

        Collections.shuffle(b);


        cards.rewind().mark();
        for (Integer aInteger : b)
            cards.put(aInteger);


        cards.rewind().mark();
        if (Seat.test) {
            dump();
            Logger.getAnonymousLogger().info(String.valueOf(CardUtil.toChar((IntBuffer) cards.duplicate().reset())));
        }
    }

    public void clear() {
        cards.rewind().mark();
    }

    public int deal() {
        return cards.get();
    }

    public void burn() {
        deal();
    }

    public IntBuffer getCards() {
        return cards.slice();
    }

    public void setCards(IntBuffer cards) {
        this.cards = cards;
    }

    public String dump() {
        final String[] strings = new String[Deck.DECK_SIZE];
        for (int i = 0; i < strings.length; i++)
            strings[i] = Integer.toHexString(deck.get(i));


        final String s = Arrays.toString(strings);
        Logger.getAnonymousLogger().info(s);
        return s;
    }
}
