package poker.eval;

import poker.player.*;

import java.util.*;
import java.util.logging.*;

public final class Deck {

    public static final int FACES_LEN = (int) Face.values().length;
    public static final int SUITS_LEN = (int) Suit.values().length;
    public static final int DECK_SIZE = FACES_LEN * SUITS_LEN;
    static final char[] faces = new char[Face.values().length];
    static final char[] suits = new char[Suit.values().length];
    static private final IntArrayCircularBuffer deck = IntArrayCircularBuffer.allocate(DECK_SIZE);
    private IntArrayCircularBuffer cards = IntArrayCircularBuffer.allocate(DECK_SIZE);

    static {
        initFaces();
        initSuits();

        final int[] seed = deck.array();

        int curs = 0;
        for (int f = 0; f < FACES_LEN; f++) {
            for (int s = 0; s < SUITS_LEN; s++) {
                final int v = f << 16 | s & 0x3;
                seed[curs++] = v;
            }
        }

        if (Seat.test) {
            deck.rewind().limit(DECK_SIZE);
            Logger.getAnonymousLogger().info(String.valueOf(CardUtil.toChar(deck)));
        }
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

    public void shuffle() {
        if (Seat.test) return;

        final int[] ints = deck.array();

        List<Integer> b = new ArrayList<Integer>();
        for (int aInteger : ints)
            b.add(aInteger);

        Collections.shuffle(b);

        cards.rewind().limit(DECK_SIZE);
        for (Integer aInteger : b)
            cards.put(aInteger);

        cards.rewind().limit(DECK_SIZE);
        if (Seat.test) {
            dump();
            Logger.getAnonymousLogger().info(String.valueOf(CardUtil.toChar(cards.duplicate().rewind().limit(DECK_SIZE))));
        }
    }

    public void clear() {
        cards.rewind().limit(DECK_SIZE);
    }

    public int deal() {
        return cards.get();
    }

    public void burn() {
        deal();
    }

    public IntArrayCircularBuffer getCards() {
        IntArrayCircularBuffer slice = cards.slice();
        slice.limit(DECK_SIZE);
        return slice;
    }

    public void setCards(IntArrayCircularBuffer cards) {
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
